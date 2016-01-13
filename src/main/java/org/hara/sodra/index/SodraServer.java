/*
 * Copyright Phani Chaitanya Vempaty
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hara.sodra.index;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.Cell;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.request.CoreAdminRequest.Create;
import org.apache.solr.client.solrj.request.CoreAdminRequest.Unload;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.SolrInputDocument;
import org.hara.sodra.utils.SodraUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Phani Chaitanya Vempaty
 *
 */
public class SodraServer {

	private SolrClient client;
	private Path solrHome = Paths.get("/Users/pvempaty/work/projects/solr/solr-5.3.1/solr/server/solr");
	private CFMetaData metadata;
	private Integer id = 0;
	private String indexName;

	public SodraServer(CFMetaData metadata) {
		this.metadata = metadata;
		client = new HttpSolrClient("http://localhost:7983/solr");
	}

	public void createIndex(String indexName, Collection<ColumnDefinition> columns)
			throws SolrServerException, IOException {
		this.indexName = indexName;
		Path solrCorePath = SodraUtils.getSolrCorePath(solrHome, indexName);
		if (solrCorePath.toFile().exists()) {
			return;
		}
		SodraUtils.createSolrCoreDirs(solrHome, indexName);
		CoreAdminResponse createCoreRsp = Create.createCore(indexName, indexName, client);
		if (createCoreRsp.getStatus() != 0) {
			throw new SolrServerException("Could not create core : " + indexName);
		}
		try {
			updateSchemaConfigFile(indexName, columns);
		} catch (Exception e) {
			// delete the index as we encountered exception
			deleteIndex(indexName);
			throw new SolrServerException(e.getMessage(), e);
		}
	}

	public String getIndexName() {
		return indexName;
	}

	protected void updateSchemaConfigFile(String indexName, Collection<ColumnDefinition> columns) throws Exception {
		Path corePath = Paths.get(solrHome.toString(), indexName);
		Path schemaXML = Paths.get(corePath.toString(), "conf", "schema.xml");
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = docBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(schemaXML.toFile());
		Element rootNode = document.getDocumentElement();
		// TODO: check if fields already exist before adding
		ColumnDefinition idColumn = null;
		for (ColumnDefinition cd : columns) {
			Element field = document.createElement("field");
			NamedNodeMap attributes = field.getAttributes();
			Attr name = document.createAttribute("name");
			Attr type = document.createAttribute("type");
			Attr indexed = document.createAttribute("indexed");
			Attr stored = document.createAttribute("stored");
			Attr required = document.createAttribute("required");
			Attr multi = document.createAttribute("multiValued");

			name.setValue(cd.name.toString());
			type.setValue(CassandraToSodraTypeMapper.getSodraType(cd.type));
			indexed.setValue("true");
			if (cd.isPrimaryKeyColumn()) {
				idColumn = cd;
				stored.setValue("true");
			} else {
				stored.setValue("false");
			}
			required.setValue("true");
			multi.setValue("false");

			attributes.setNamedItem(name);
			attributes.setNamedItem(type);
			attributes.setNamedItem(indexed);
			attributes.setNamedItem(stored);
			attributes.setNamedItem(required);
			attributes.setNamedItem(multi);

			rootNode.appendChild(field);
		}

		// update unique key
		NodeList nodeList = document.getElementsByTagName("uniqueKey");
		if (nodeList.getLength() != 1) {
			throw new SolrException(ErrorCode.SERVER_ERROR, "Should have 1 <uniqueKey> node in schema.xml");
		}
		Node uniqueKey = nodeList.item(0);
		uniqueKey.setTextContent(idColumn.name.toString());

		// save the updated file
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult result = new StreamResult(schemaXML.toFile());
		DOMSource source = new DOMSource(document);
		transformer.transform(source, result);

		CoreAdminRequest.reloadCore(indexName, client);
	}

	public void deleteIndex(String indexName) throws SolrServerException, IOException {
		CoreAdminResponse unloadCore = Unload.unloadCore(indexName, client);
		if (unloadCore.getStatus() != 0) {
			throw new SolrServerException("Could not unload core : " + indexName);
		}
		SodraUtils.deleteSolrCore(solrHome, indexName);
	}

	public void index(DecoratedKey key, ColumnFamily cf) throws SolrServerException, IOException {
		try {
			SolrInputDocument doc = new SolrInputDocument();
			if (cf.iterator().hasNext()) {
				for (Cell cell : cf) {
					ByteBuffer value = cell.value();
					CellName cellName = cell.name();
					ColumnDefinition columnDefinition = metadata.getColumnDefinition(cellName);
					if (columnDefinition == null) {
						continue;
					}
					String fieldName = columnDefinition.name.toString();
					AbstractType<?> type = columnDefinition.type;
					Object composedValue = null;
					if (type.asCQL3Type() == CQL3Type.Native.INT) {
						composedValue = ((Int32Type) type).compose(value);
					} else if (type.asCQL3Type() == CQL3Type.Native.TEXT) {
						composedValue = ((UTF8Type) type).compose(value);
					}
					doc.addField(fieldName, composedValue);
				}
				// TODO: get the primary key value directly from the field
				id++;
				doc.addField("user_id", id);
			}
			client.add(indexName, doc);
			client.commit(indexName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delete(DecoratedKey key) throws SolrServerException, IOException {
		String userId = Integer.toString(Int32Type.instance.compose(key.getKey()));
		client.deleteById(indexName, userId);
		client.commit(indexName);
	}

	public SolrDocumentList search(String query) throws SolrServerException, IOException {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery(query);
		QueryResponse response = client.query(indexName, solrQuery);
		return response.getResults();
	}
	
	public static void main(String[] args) {
		new SodraServer(null);
	}

}
