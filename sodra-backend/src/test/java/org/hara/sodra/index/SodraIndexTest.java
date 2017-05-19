package org.hara.sodra.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.palantir.docker.compose.DockerComposeRule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

/*
 * Copyright Phani Chaitanya Vempaty
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
public class SodraIndexTest {

  @ClassRule
  public static DockerComposeRule docker = DockerComposeRule.builder().file(
      "src/test/resources/docker-compose.yml").build();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    docker.dockerCompose().up();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    docker.dockerCompose().down();
  }

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testAll() throws IOException, InterruptedException {
    String ip = docker.containers().container("sodra").port(9042).getIp();
    Cluster cluster = Cluster.builder().withClusterName("Test Cluster").addContactPoint(ip).build();
    Session session = cluster.connect();
    String keyspace = "sodra_tests";
    String table = "users";
    createKeyspace(session, keyspace);
    session = cluster.connect(keyspace); // get a new session with keyspace now
    createTable(session, table);
    insertRows(session, table);
    createSodraIndex(session, table);
    search(session, table);
  }

  private void createKeyspace(Session session, String keyspace) {
    ResultSet resultSet = session.execute("CREATE KEYSPACE " + keyspace
        + " WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }");
    assertTrue(resultSet.isExhausted());
    assertTrue(resultSet.getExecutionInfo().isSchemaInAgreement());
  }

  private void createTable(Session session, String table) {
    ResultSet resultSet = session.execute("CREATE TABLE " + table
        + " ( id int primary key, username text, fullname text, data text )");
    assertTrue(resultSet.isExhausted());
    assertTrue(resultSet.getExecutionInfo().isSchemaInAgreement());
  }

  private void insertRows(Session session, String table) {
    ResultSet resultSet = session.execute("INSERT INTO " + table
        + " ( id, username, fullname, data) VALUES (4, 'redragons', 'Red Dragons', 'Some random data to insert here')");
    assertTrue(resultSet.isExhausted());
    assertTrue(resultSet.getExecutionInfo().isSchemaInAgreement());
  }

  private void createSodraIndex(Session session, String table) {
    ResultSet resultSet = session.execute("CREATE CUSTOM INDEX user_idx ON " + table
        + "(data) USING 'org.hara.sodra.index.SodraIndex'");
    assertTrue(resultSet.isExhausted());
    assertTrue(resultSet.getExecutionInfo().isSchemaInAgreement());
  }

  private void search(Session session, String table) {
    try {
      Thread.sleep(5000); // TODO: remove this once issue #29 is fixed
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    ResultSet resultSet = session.execute("SELECT * FROM " + table
        + " where data = 'data:some OR username:redragons'");
    assertTrue(resultSet.getExecutionInfo().isSchemaInAgreement());
    List<Row> rows = resultSet.all();
    assertTrue(resultSet.isExhausted());
    assertEquals(1, rows.size());
    Row row = rows.get(0);
    int id = row.getInt(0);
    assertEquals(4, id);
  }

}
