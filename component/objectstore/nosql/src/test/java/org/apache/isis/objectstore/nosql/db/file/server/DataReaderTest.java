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

package org.apache.isis.objectstore.nosql.db.file.server;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;

import org.junit.BeforeClass;
import org.junit.Test;

public class DataReaderTest {

    private DataFileReader reader;

    @BeforeClass
    public static void setUp() throws Exception {
        Util.setDirectory("target/test", "services", "logs", "archive");
        Util.ensureDirectoryExists();
        new File("target/test/type").mkdir();
    }

    @Test
    public void noFileCausesException() throws Exception {
        try {
            new DataFileReader("type", "nonexistant");
            fail();
        } catch (final FileNotFoundException expected) {
        }
    }

    @Test
    public void noDataRead() throws Exception {
        final FileWriter writer = new FileWriter("target/test/type/0013.data");
        writer.write("");
        writer.close();

        try {
            reader = new DataFileReader("type", "0013");
            fail();
        } catch (final FileServerException expected) {
            assertThat(expected.getMessage(), startsWith("No data in file:"));
        }

    }

    @Test
    public void readIdAndVersion() throws Exception {
        final FileWriter writer = new FileWriter("target/test/type/0012.data");
        writer.write("class.type 0012 17\n{data}");
        writer.close();

        reader = new DataFileReader("type", "0012");

        assertEquals("0012", reader.getId());
        assertEquals("17", reader.getVersion());

        final String data = reader.getData();
        assertEquals("{data}\n", data);
        reader.close();
    }
}
