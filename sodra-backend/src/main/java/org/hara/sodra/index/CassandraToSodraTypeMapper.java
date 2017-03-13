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

package org.hara.sodra.index;

import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.BooleanType;
import org.apache.cassandra.db.marshal.ByteType;
import org.apache.cassandra.db.marshal.BytesType;
import org.apache.cassandra.db.marshal.DateType;
import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.FloatType;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.IntegerType;
import org.apache.cassandra.db.marshal.LongType;
import org.apache.cassandra.db.marshal.TimestampType;
import org.apache.cassandra.db.marshal.UTF8Type;

/**
 * @author Phani Chaitanya Vempaty
 *
 */
public class CassandraToSodraTypeMapper {

	public static String getSolrType(AbstractType<?> cassandraType) {
		if (cassandraType.getClass() == BooleanType.class) {
			return "boolean";
		} else if (cassandraType.getClass() == UTF8Type.class) {
			return "text_en";
		} else if (cassandraType.getClass() == Int32Type.class || cassandraType.getClass() == IntegerType.class) {
			return "int";
		} else if (cassandraType.getClass() == LongType.class) {
			return "long";
		} else if (cassandraType.getClass() == ByteType.class || cassandraType.getClass() == BytesType.class) {
			return "binary";
		} else if (cassandraType.getClass() == FloatType.class) {
			return "float";
		} else if (cassandraType.getClass() == DateType.class || cassandraType.getClass() == TimestampType.class) {
			return "tdate";
		} else if (cassandraType.getClass() == DoubleType.class) {
			return "double";
		} else if (cassandraType.getClass() == FloatType.class) {
			return "float";
		}
		return "text_general";
	}

}
