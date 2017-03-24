# Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Build](#build)
4. [Distribution](#distribution)
5. [Testing](#testing)

## Overview

Sodra is Cassandra + Solr together running in the same JVM so that you don't need to have two java processes running. It also exposes Solr's Rest API for that node.

## Prerequisites

* Docker (required only for testing or some non-ideal deployment setup)
* Java >= 1.8
* Bash >= 4

## Build

* Building the sodra jar

> ./gradlew sodra-backend:shadowJar

## Distribution

> ./gradlew sodra-backend:zip

## Testing

> unzip sodra-backend-0.1.zip
> cd sodra
> ./setup
> docker run -it sodra:latest
