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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LogWriterTest {

    private LogWriter logger;
    private File logFile1;
    private File logFile2;
    private List<FileContent> items;

    @BeforeClass
    public static void directory() {
        Util.setDirectory("target/test", "services", "logs", "archive");
        Util.ensureDirectoryExists();
    }

    @Before
    public void setUp() throws Exception {
        logFile1 = new File("target/test/logs", "recovery0.log");
        logFile1.delete();
        assertFalse(logFile1.exists());
        logFile2 = new File("target/test/logs", "recovery1.log");
        logFile2.delete();
        assertFalse(logFile2.exists());

        logger = new LogWriter();
        logger.startup();

        items = new ArrayList<FileContent>();
        items.add(new FileContent('U', "20", "6", "7", "type", "{data}"));
        new DataFileWriter(items);
    }

    @After
    public void tearDown() throws Exception {
        if (logger != null) {
            logger.shutdown();
        }
    }

    @Test
    public void newLogFileCreatedOnStartup() throws Exception {
        assertTrue(logFile1.exists() && logFile1.length() == 0);
        assertFalse(logFile2.exists());
    }

    @Test
    public void logsData() throws Exception {
        logger.logWrites(items);

        final BufferedReader reader = new BufferedReader(new FileReader(logFile1));
        String line = reader.readLine();
        line = reader.readLine();
        Assert.assertEquals("Utype 20 7", line);
        line = reader.readLine();
        Assert.assertEquals("{data}", line);
        reader.close();

    }

    @Test
    public void logAddedToExistingFile() throws Exception {
        logger.logWrites(items);
        assertTrue(logFile1.exists() && logFile1.length() > 0);
        assertFalse(logFile2.exists());
    }

    @Test
    public void logAddedToNewFileWhenRotated() throws Exception {
        logger.logWrites(items);
        logger.startNewFile();
        logger.logWrites(items);
        assertTrue(logFile2.exists() && logFile1.length() > 0);
    }
}
