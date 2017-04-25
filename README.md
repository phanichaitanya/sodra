[![Build Status](https://travis-ci.org/redragons/sodra.svg?branch=master)](https://travis-ci.org/redragons/sodra)
[![codecov](https://codecov.io/gh/redragons/sodra/branch/master/graph/badge.svg)](https://codecov.io/gh/redragons/sodra)

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

> ./gradlew build

## Distribution

> ./gradlew sodra-backend:zip

Zip file will be located under "//sodra/sodra-backend/build/distributions"

## Testing

> * unzip sodra-backend-0.1.zip
> * cd sodra
> * ./setup
> * docker exec -it sodra bash

## Examples

Once you are in the docker container:

> cd /sodra_install/examples

> cqlsh -f users
