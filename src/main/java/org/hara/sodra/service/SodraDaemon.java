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

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.cassandra.service.CassandraDaemon;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.hara.sodra.SodraConfig;
import org.hara.sodra.utils.SodraUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * @author Phani Chaitanya Vempaty
 *
 */
public class SodraDaemon extends CassandraDaemon {

	private static SodraConfig sodraConfig;
	
	public static Integer solrPort = 8983;

	static {
		try {
			loadConfig();
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public static SodraConfig getSodraConfig() {
		return sodraConfig;
	}

	private static void loadConfig() throws Exception {
		String cassandraHome = System.getenv("CASSANDRA_HOME");
		if (cassandraHome == null) {
			throw new Exception("CASSANDRA_HOME environment variable is not defined");
		}
		Path sodraConfigPath = Paths.get(cassandraHome, "sodra_conf", "sodra.yaml");
		File sodraConfigFile = sodraConfigPath.toFile();
		if (!sodraConfigFile.exists()) {
			throw new Exception("sodra.yaml does not exist inside sodra_conf dir under cassandra home");
		}
		FileInputStream fis = new FileInputStream(sodraConfigFile);
		Yaml yaml = new Yaml(new Constructor(SodraConfig.class));
		sodraConfig = yaml.loadAs(fis, SodraConfig.class);
	}

	private static final SodraDaemon instance = new SodraDaemon();
	private JettySolrRunner solrServer;

	public void startSodra() throws Exception {
		String context = "/solr";
		String solrHome = SodraUtils.getSolrHome().toString();
		solrServer = new JettySolrRunner(solrHome, context, SodraDaemon.getSodraConfig().solr_port);
		solrServer.start();
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

	/**
	 * must start solr jetty first before activating cassandra daemon
	 */
	public void activateSodra() {
		try {
			instance.startSodra();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		instance.activateSodra();
		instance.activate();
	}

}
