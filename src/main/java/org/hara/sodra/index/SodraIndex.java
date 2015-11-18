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
import java.util.Set;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.index.PerRowSecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.utils.concurrent.OpOrder.Group;
import org.apache.solr.client.solrj.SolrServerException;
import org.hara.sodra.search.SodraIndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Phani Chaitanya Vempaty
 *
 */
public class SodraIndex extends PerRowSecondaryIndex {

	private static final Logger LOGGER = LoggerFactory.getLogger(SodraIndex.class);

	private String indexName;

	private IPartitioner partitioner;

	private CFMetaData metadata;

	private SodraServer sodraServer;

	@Override
	public void index(ByteBuffer rowKey, ColumnFamily cf) {
		System.out.println("Getting index call");
		DecoratedKey decorateKey = partitioner.decorateKey(rowKey);
		try {
			sodraServer.index(decorateKey, cf);
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void delete(DecoratedKey key, Group opGroup) {
	}

	@Override
	public void init() {
		LOGGER.info("Initializing Sodra Index");
		indexName = baseCfs.name;
		partitioner = baseCfs.partitioner;
		metadata = Schema.instance.getCFMetaData(baseCfs.keyspace.getName(), baseCfs.name);
		sodraServer = new SodraServer(metadata);
		try {
			// do not create the index if it already exists ?
			sodraServer.createIndex(indexName, baseCfs.metadata.allColumns());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void reload() {
	}

	@Override
	public void validateOptions() throws ConfigurationException {
	}

	@Override
	public String getIndexName() {
		return indexName;
	}

	@Override
	protected SecondaryIndexSearcher createSecondaryIndexSearcher(Set<ByteBuffer> columns) {
		return new SodraIndexSearcher(baseCfs.indexManager, columns, sodraServer);
	}

	@Override
	public void forceBlockingFlush() {
		// TODO: issue a commit on solr index
	}

	@Override
	public ColumnFamilyStore getIndexCfs() {
		return baseCfs;
	}

	@Override
	public void removeIndex(ByteBuffer columnName) {
		try {
			sodraServer.deleteIndex(indexName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void invalidate() {
	}

	@Override
	public void truncateBlocking(long truncatedAt) {
	}

	@Override
	public boolean indexes(CellName name) {
		return true;
	}

	@Override
	public long estimateResultRows() {
		return 1;
	}

}
