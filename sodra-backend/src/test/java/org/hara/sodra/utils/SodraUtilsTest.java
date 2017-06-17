package org.hara.sodra.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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
@RunWith(JUnit4.class)
public class SodraUtilsTest {

  @Rule
  public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  private static final String TEST_SODRA_HOME = "/tmp/sodra/data/solr";

  @Before
  public void setUp() throws Exception {
    this.environmentVariables.set(SodraConstants.SODRA_DATA_DIR, "/tmp/sodra/data");
    Path solrHome = SodraUtils.getSolrHome();
    Files.createDirectories(solrHome);
    Path solrTemplateConfDir = Paths
        .get(solrHome.getParent().toString(), "index_template_config", "conf");
    Files.createDirectories(solrTemplateConfDir);
    Files.createTempFile(solrTemplateConfDir, "prefix.", ".suffix");
  }

  @After
  public void tearDown() throws Exception {
    Path solrHome = Paths.get(TEST_SODRA_HOME);
    FileUtils.deleteDirectory(solrHome.getParent().toFile());
  }

  @Test
  public void testSolrHome() {
    Path solrHome = SodraUtils.getSolrHome();
    assertEquals("/tmp/sodra/data/solr", solrHome.toString());
  }

  @Test(expected = RuntimeException.class)
  public void testNoSolrHome() {
    this.environmentVariables.set(SodraConstants.SODRA_DATA_DIR, null);
    Path solrHome = SodraUtils.getSolrHome();
    assertEquals("/tmp/sodra/data/solr", solrHome.toString());
  }

  @Test
  public void testSolrCorePath() {
    Path corePath = SodraUtils.getSolrCorePath(SodraUtils.getSolrHome(), "testIndex");
    assertEquals("/tmp/sodra/data/solr/testIndex", corePath.toString());
  }

  @Test
  public void testCopySolrConfigs() throws IOException {
    Path solrHome = SodraUtils.getSolrHome();
    Path solrTemplateConfDir = Paths.get(solrHome.getParent().toString(), "index_template_config");
    Path toTemplateConfDir = Paths.get(TEST_SODRA_HOME, "index_template_config_to");
    SodraUtils.copySolrConfigs(solrTemplateConfDir, toTemplateConfDir);
    assertTrue(toTemplateConfDir.toFile().exists());
    assertEquals(toTemplateConfDir.toFile().list().length, 1);
  }

  @Test
  public void testCreateSolrCoreDirs() throws IOException {
    Path solrHome = SodraUtils.getSolrHome();
    SodraUtils.createSolrCoreDirs(solrHome, "testIndex");
    assertTrue(Paths.get(solrHome.toString(), "testIndex").toFile().exists());
  }

  @Test
  public void testDeleteCore() throws IOException {
    Path solrHome = SodraUtils.getSolrHome();
    SodraUtils.deleteSolrCore(solrHome, "testIndex");
    assertFalse(Paths.get(solrHome.toString(), "testIndex").toFile().exists());
  }

}
