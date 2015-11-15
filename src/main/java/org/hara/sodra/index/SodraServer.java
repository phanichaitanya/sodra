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

import org.apache.cassandra.config.ColumnDefinition;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest.Create;
import org.apache.solr.client.solrj.request.CoreAdminRequest.Unload;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
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

	public static final SodraServer instance = new SodraServer();
	private SolrClient client;
	private Path solrHome = Paths.get("/Users/pvempaty/work/projects/solr-5.3.1/solr/server/solr");

	public SodraServer() {
		client = new HttpSolrClient("http://localhost:8983/solr");
	}

	public void createIndex(String indexName, Collection<ColumnDefinition> columns)
			throws SolrServerException, IOException {
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
	}

	public void deleteIndex(String indexName) throws SolrServerException, IOException {
		CoreAdminResponse unloadCore = Unload.unloadCore(indexName, client);
		if (unloadCore.getStatus() != 0) {
			throw new SolrServerException("Could not unload core : " + indexName);
		}
		SodraUtils.deleteSolrCore(solrHome, indexName);
	}
	
	public static void main(String[] args) {
	}

}
