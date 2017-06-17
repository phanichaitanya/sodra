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

package org.hara.sodra.search;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.IndexExpression;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.index.SecondaryIndexManager;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hara.sodra.index.SodraServer;

/**
 * @author Phani Chaitanya Vempaty
 */
public class SodraIndexSearcher extends SecondaryIndexSearcher {

  private SodraServer sodraServer;

  public SodraIndexSearcher(SecondaryIndexManager indexManager, Set<ByteBuffer> columns,
      SodraServer sodraServer) {
    super(indexManager, columns);
    this.sodraServer = sodraServer;
  }

  @Override
  public List<Row> search(ExtendedFilter filter) {
    List<Row> rows = Lists.newArrayList();
    IndexExpression indexExpression = filter.getClause().get(0);
    String query = UTF8Type.instance.compose(indexExpression.value);
    try {
      SolrDocumentList results = sodraServer.search(query);
      ColumnDefinition idColumn = sodraServer.getIdColumn();
      if (idColumn == null) {
        throw new IOException("No id column for index : " + sodraServer.getIndexName());
      }
      String idField = idColumn.name.toString();
      for (SolrDocument doc : results) {
        Integer id = (Integer) doc.getFieldValue(idField);
        ByteBuffer decomposedId = Int32Type.instance.decompose(id);
        DecoratedKey decorateKey = baseCfs.partitioner.decorateKey(decomposedId);
        QueryFilter queryFilter = QueryFilter
            .getIdentityFilter(decorateKey, sodraServer.getIndexName(),
                filter.timestamp);
        ColumnFamily columnFamily = baseCfs.getColumnFamily(queryFilter);
        if (columnFamily == null) {
          continue;
        }
        Row row = new Row(decomposedId, columnFamily);
        rows.add(row);
      }
    } catch (SolrServerException | IOException e) {
      e.printStackTrace();
    }
    return rows;
  }

  @Override
  public List<Row> postReconciliationProcessing(List<IndexExpression> clause, List<Row> rows) {
    return rows;
  }

}
