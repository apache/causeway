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


package org.apache.isis.alternatives.objectstore.nosql.file.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.isis.alternatives.objectstore.nosql.file.server.DataWriter;
import org.apache.isis.alternatives.objectstore.nosql.file.server.FileContent;
import org.apache.isis.alternatives.objectstore.nosql.file.server.Util;
import org.junit.Before;
import org.junit.Test;

public class DataWriterTest {

    @Before
    public void setUp() throws Exception {
        Util.setDirectory("target/test", "services", "logs");
        Util.ensureDirectoryExists();
    }

    @Test
    public void testWriteData() throws Exception {
        File file = new File("target/test/org.domain.Class/1030.data");
        file.mkdirs();
        file.createNewFile();
        Assert.assertTrue(file.exists());
        
        List<FileContent> files = new ArrayList<FileContent>();
        files.add(new FileContent('I', "1023", "1", "2",  "org.domain.Class", "{data1}"));
        files.add(new FileContent('U', "1024", "21", "22", "org.domain.Class", "{data2}"));
        files.add(new FileContent('D', "1030", "66", "", "org.domain.Class", ""));
        DataWriter writer = new DataWriter(files);
        writer.writeData();
        
        BufferedReader reader = new BufferedReader(new FileReader("target/test/org.domain.Class/1023.data"));
        Assert.assertEquals("org.domain.Class 1023 2", reader.readLine());
        Assert.assertEquals("{data1}", reader.readLine());

        reader = new BufferedReader(new FileReader("target/test/org.domain.Class/1024.data"));
        Assert.assertEquals("org.domain.Class 1024 22", reader.readLine());
        Assert.assertEquals("{data2}", reader.readLine());
        
        Assert.assertFalse("file still exists", file.exists());
    }


    @Test
    public void createsTypeDirectory() throws Exception {
        File file = new File("target/test/org.domain.Class");
        for( File f: file.listFiles()) {
            f.delete();
        }
        file.delete();
        Assert.assertFalse(file.exists());
        

        List<FileContent> files = new ArrayList<FileContent>();
        files.add(new FileContent('I', "1023", "1", "2",  "org.domain.Class", "{data1}"));
        DataWriter writer = new DataWriter(files);
        writer.writeData();
        
        Assert.assertTrue(file.exists());
    }

}


