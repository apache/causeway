/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.objectstore.nosql.db.mongo;

import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class DemoMongo {
    
    // @Test
    public void installed() throws Exception {

        final Mongo m = new Mongo();

        for (final String s : m.getDatabaseNames()) {
            System.out.println(s);
        }

        /*
         * Mongo m = new Mongo( "localhost" ); Mongo m = new Mongo( "localhost"
         * , 27017 );
         */
        m.dropDatabase("mydb");

        System.out.println("\n...");
        for (final String s : m.getDatabaseNames()) {
            System.out.println(s);
        }

        final DB db = m.getDB("mydb");
        /*
         * DBCollection coll = db.getCollection("testCollection1"); coll =
         * db.getCollection("testCollection2");
         */

        final DBCollection coll = db.getCollection("testCollection1");

        final BasicDBObject doc = new BasicDBObject();

        doc.put("name", "MongoDB");
        doc.put("type", "database");
        doc.put("count", 1);

        final BasicDBObject info = new BasicDBObject();

        info.put("x", 203);
        info.put("y", 102);

        doc.put("info", info);

        coll.insert(doc);

        final Set<String> colls = db.getCollectionNames();

        for (final String s : colls) {
            System.out.println(s);
        }

    }
}
