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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataWriterTest {

    private static final String TARGET_DIRECTORY = "target/test/";
    private static final String FULLY_QUALIFIED_CLASSNAME = "org.domain.Class";
    private static final String FULLY_QUALIFIED_CLASSNAME_2 = "org.domain.Class2";
    private DataFileWriter writer;

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
        Util.setDirectory(TARGET_DIRECTORY, "services", "logs", "archive");
        Util.ensureDirectoryExists();
    }

    @After
    public void tearDown() throws Exception {
        if (writer != null) {
            writer.close();
        }
    }

    @Test
    public void testWriteData() throws Exception {
        final File file = new File(TARGET_DIRECTORY + FULLY_QUALIFIED_CLASSNAME + "/1030.data");
        file.mkdirs();
        file.createNewFile();
        Assert.assertTrue(file.exists());

        final List<FileContent> files = new ArrayList<FileContent>();
        files.add(new FileContent('I', "1023", "1", "2", FULLY_QUALIFIED_CLASSNAME, "{data1}"));
        files.add(new FileContent('U', "1024", "21", "22", FULLY_QUALIFIED_CLASSNAME, "{data2}"));
        files.add(new FileContent('D', "1030", "66", "", FULLY_QUALIFIED_CLASSNAME, ""));
        final DataFileWriter writer = new DataFileWriter(files);
        writer.writeData();

        BufferedReader reader = new BufferedReader(new FileReader(TARGET_DIRECTORY + FULLY_QUALIFIED_CLASSNAME + "/1023.data"));
        Assert.assertEquals("org.domain.Class 1023 2", reader.readLine());
        Assert.assertEquals("{data1}", reader.readLine());

        reader = new BufferedReader(new FileReader(TARGET_DIRECTORY + FULLY_QUALIFIED_CLASSNAME + "/1024.data"));
        Assert.assertEquals(FULLY_QUALIFIED_CLASSNAME + " 1024 22", reader.readLine());
        Assert.assertEquals("{data2}", reader.readLine());

        Assert.assertFalse("file still exists", file.exists());
    }

    @Test
    public void createsTypeDirectory() throws Exception {
        final String dir = TARGET_DIRECTORY + FULLY_QUALIFIED_CLASSNAME_2;
        final File file = deleteDirectory(dir);
        Assert.assertFalse(file.exists());

        final List<FileContent> files = new ArrayList<FileContent>();
        files.add(new FileContent('I', "1023", "1", "2", FULLY_QUALIFIED_CLASSNAME_2, "{data1}"));
        writer = new DataFileWriter(files);
        writer.writeData();

        Assert.assertTrue(file.exists());
    }

    protected File deleteDirectory(final String dir) {
        final File file = new File(dir);
        if (file.exists()) {
            for (final File f : file.listFiles()) {
                f.delete();
            }
            file.delete();

        }
        return file;
    }

}
