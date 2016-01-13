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

package org.hara.sodra.service;

import org.apache.cassandra.service.CassandraDaemon;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;

/**
 * @author Phani Chaitanya Vempaty
 *
 */
public class SodraDaemon extends CassandraDaemon {

	private static final SodraDaemon instance = new SodraDaemon();
	private JettySolrRunner solrServer;

	public void startSodra() throws Exception {
		int port = 7983;
		String context = "/solr";
		String solrHome = "/Users/pvempaty/work/projects/solr/solr-5.3.1/solr/server/solr";
		solrServer = new JettySolrRunner(solrHome, context, port);
		solrServer.start();
	}

	@Override
	public void start() {
		// must start solr jetty first and then cassandra
		try {
			startSodra();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.start();
	}

	@Override
	public void stop() {
		try {
			solrServer.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.stop();
	}

	public static void main(String[] args) {
		instance.activate();
	}

}
