# Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Build](#build)
4. [Distribution](#distribution)
5. [Testing](#testing)
6. [Examples](#examples)

## Overview

Sodra is Cassandra + Solr together running in the same JVM so that you don't need to have two java processes running. It also exposes Solr's Rest API for that node.

## Prerequisites

* Docker (required only for testing or some non-ideal deployment setup)
* Java >= 1.7
* Bash >= 4

## Build

> ./gradlew sodra-backend:shadowJar

## Distribution

> ./gradlew sodra-backend:zip

## Testing

> * unzip sodra-backend-0.1.zip
> * cd sodra
> * ./setup
> * docker run -it sodra:latest

## Examples

* Create a keyspace

> CREATE KEYSPACE IF NOT EXISTS sodra <br/>
        WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
        
* Create a table

> CREATE TABLE sodra.user ( <br/>
        id int PRIMARY KEY, <br/>
        username text, <br/>
        fullname text, <br/>
        data text <br/>
    );
    
* Insert some data

> INSERT INTO sodra.user (id, username, fullname, data) <br/>
        VALUES (1, 'redragons', 'Red Dragons', 'Some random data to insert here');
        
* Create sodra index (solr)

> CREATE CUSTOM INDEX user_idx ON sodra.user(data) <br/>
        USING 'org.hara.sodra.index.SodraIndex';
        
* Sample queries

> SELECT * FROM sodra.user where data = 'data:some OR username:redragons';