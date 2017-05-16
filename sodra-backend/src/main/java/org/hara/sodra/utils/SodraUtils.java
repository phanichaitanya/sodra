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

package org.hara.sodra.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

/**
 * @author Phani Chaitanya Vempaty
 *
 */
public class SodraUtils {

	public static final Path getSolrCorePath(Path solrHome, String indexName) {
		Path corePath = Paths.get(solrHome.toString(), indexName);
		return corePath;
	}

	public static final Path getSolrHome() {
		String sodraDataDir = System.getenv(SodraConstants.SODRA_DATA_DIR);
		if (sodraDataDir == null) {
			throw new RuntimeException("SODRA_DATA_DIR environment variable is not defined");
		}
		// TODO: read the cassandra.yaml file and construct the solr home based
		// on data dir
		Path solrHome = Paths.get(sodraDataDir, "solr");
		return solrHome;
	}

	public static final void createSolrCoreDirs(Path solrHome, String indexName) throws IOException {
		Path corePath = getSolrCorePath(solrHome, indexName);
		Files.createDirectory(corePath);
		Path coreConfPath = Paths.get(corePath.toString(), "conf");
		Path baseConfPath = Paths.get(solrHome.getParent().toString(), "index_template_config", "conf");
		copySolrConfigs(baseConfPath, coreConfPath);
	}

	public static final void copySolrConfigs(Path fromConfDir, Path toConfDir) throws IOException {
		FileUtils.copyDirectory(fromConfDir.toFile(), toConfDir.toFile());
	}

	public static final void deleteSolrCore(Path solrHome, String indexName) throws IOException {
		Path corePath = Paths.get(solrHome.toString(), indexName);
		FileUtils.deleteDirectory(corePath.toFile());
	}

}
