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
	
	public static final void createSolrCoreDirs(Path solrHome, String indexName) throws IOException {
		Path corePath = getSolrCorePath(solrHome, indexName);
		Files.createDirectory(corePath);
		Path coreConfPath = Paths.get(corePath.toString(), "conf");
		Path baseConfPath = Paths.get(solrHome.toString(), "configsets", "sodra_template_configs", "conf");
		copySolrConfigs(baseConfPath, coreConfPath);
	}

	public static final void copySolrConfigs(Path fromConfDir, Path toConfDir) throws IOException {
		FileUtils.copyDirectory(fromConfDir.toFile(), toConfDir.toFile());
	}

	public static final void deleteSolrCore(Path solrHome, String indexName) throws IOException {
		Path corePath = Paths.get(solrHome.toString(), indexName);
		FileUtils.deleteDirectory(corePath.toFile());
	}

	public static void main(String[] args) {
		Path solrHome = Paths.get("/Users/pvempaty/work/projects/solr-5.3.1/solr/server/solr");
		try {
			createSolrCoreDirs(solrHome, "tmp");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
