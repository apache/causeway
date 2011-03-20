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

package org.apache.isis.runtimes.dflt.objectstores.nosql.file.server;

import static org.apache.isis.core.commons.lang.StringUtils.lineSeparated;
import static org.apache.isis.core.commons.matchers.IsisMatchers.existsAndNotEmpty;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.isis.core.commons.lang.IoUtils;

public class FileServerTest {
    private FileServerProcessor server;
    private File logFile1;
    private File logFile2;
    private ByteArrayOutputStream out;

    @BeforeClass
    public static void setUp() throws Exception {
        Util.setDirectory("target/test", "services", "logs");
        Util.ensureDirectoryExists();
        new File("target/test/type").mkdir();
    }

    @Before
    public void startup() {
        logFile1 = recreateFile("target/test/logs", "recovery0.log");
        logFile2 = recreateFile("target/test/logs", "recovery1.log");

        server = new FileServerProcessor();
        server.startup();

        out = new ByteArrayOutputStream();
    }

    private static File recreateFile(String parent, String child) {
        File file = new File(parent, child);
        file.delete();
        assertFalse(file.exists());
        return file;
    }

    @After
    public void tearDown() throws Exception {
        if (server != null) {
            server.shutdown();
        }
    }

    @Test
    public void cantReadOrWriteAfterShutdown() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[128]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ServerConnection connection = new ServerConnection(in, out);

        server.shutdown();
        server.process(connection);
        assertThat(new String(out.toByteArray(), "utf-8"), is(equalTo(lineSeparated("abort\n"))));
    }

    @Test
    public void writeCreatesFilesUsingDataWriter() throws Exception {
        File file1 = new File("target/test/org.domain.Class", "1025.data");
        File file2 = new File("target/test/org.domain.Class", "1026.data");
        file1.delete();
        file2.delete();
        assertFalse(file1.exists());
        assertFalse(file2.exists());

        InputStream in =
            IoUtils.asUtf8ByteStream("W\nIorg.domain.Class 1025 null 1  \n{data1}\n\nIorg.domain.Class 1026 null 1\n{data2}\n");
        ServerConnection connection = new ServerConnection(in, out);
        server.process(connection);
        
        assertThat(out.toString(), is(equalTo(lineSeparated("ok\n"))));
        assertThat(file1, existsAndNotEmpty());
        assertThat(file2, existsAndNotEmpty());
    }

    @Test
    public void writeUpdatesFilesUsingDataWriter() throws Exception {
        File file2 = new File("target/test/org.domain.Class", "1026.data");
        FileWriter fileWriter = new FileWriter(file2);
        String originalData = "org.domain.Class 1026 21 {}";
        fileWriter.write(originalData);
        fileWriter.close();

        ServerConnection connection =
            new ServerConnection(IoUtils.asUtf8ByteStream("W\nUorg.domain.Class 1026 21 22 \n{data2}\n"), out);
        server.process(connection);
        
        assertThat(out.toString(), is(equalTo(lineSeparated("ok\n"))));
        assertThat(file2.length(), is(greaterThan((long)originalData.length())));
    }

    @Test
    public void writeUpdateFailsWhenVersionsDontMatch() throws Exception {
        File file2 = new File("target/test/org.domain.Class", "1026.data");
        FileWriter fileWriter = new FileWriter(file2);
        String originalData = "org.domain.Class 1026 21\n{datax}";
        fileWriter.write(originalData);
        fileWriter.close();

        ServerConnection connection =
            new ServerConnection(IoUtils.asUtf8ByteStream("W\nUorg.domain.Class 1026 19 21 \n{data2}\n"), out);
        server.process(connection);
        
        assertThat(out.toString(), is(equalTo(lineSeparated("error\n{datax}\n"))));
    }

    @Test
    public void writeCreatesLogFile() throws Exception {
        ServerConnection connection =
            new ServerConnection(IoUtils.asUtf8ByteStream("W\nIorg.domain.Class 1025 6 7\n{data1}\n"), out);
        server.process(connection);

        assertThat(out.toString(), is(equalTo(lineSeparated("ok\n"))));
        
        assertThat(logFile1, existsAndNotEmpty());
        assertThat(logFile2, not(existsAndNotEmpty()));
    }

    @Test
    public void readNonExistingFileThrowsException() throws Exception {
        File file1 = new File("target/test/org.domain.Class", "2020.data");
        file1.delete();
        ServerConnection connection = new ServerConnection(IoUtils.asUtf8ByteStream("Rorg.domain.Class 2020"), out);
        server.process(connection);

        String string = out.toString();
        assertThat(string, startsWith("not-found"));
        assertThat(string, containsString("File not found for org.domain.Class/2020"));
    }


    @Test
    public void aTestTheTests() throws Exception {
        File dir = new File("target/test/org.domain.Class");
        assertTrue(dir.exists());
        
        File file1 = new File("target/test/org.domain.Class", "2025.data");
        assertTrue(file1.getParentFile().exists());
        
        FileWriter fileWriter = new FileWriter(file1);
        assertNotNull(fileWriter);
        fileWriter.write("data");
        fileWriter.close();
    }

    @Test
    public void copyOfReadTest() throws Exception {
        File file1 = new File("target/test/org.domain.Class", "2025.data");
        FileWriter fileWriter = new FileWriter(file1);
        fileWriter.write("type 1025 1\n{data1}");
        fileWriter.close();

        ServerConnection connection = new ServerConnection(IoUtils.asUtf8ByteStream("Rorg.domain.Class 2025"), out);
        server.process(connection);
        
        assertThat(out.toString(), is(equalTo(lineSeparated("ok\n{data1}\n"))));
    }


    @Test
    public void read() throws Exception {
        File file1 = new File("target/test/org.domain.Class", "2025.data");
        FileWriter fileWriter = new FileWriter(file1);
        fileWriter.write("type 1025 1\n{data1}");
        fileWriter.close();

        ServerConnection connection = new ServerConnection(IoUtils.asUtf8ByteStream("Rorg.domain.Class 2025"), out);
        server.process(connection);
        
        assertThat(out.toString(), is(equalTo(lineSeparated("ok\n{data1}\n"))));
    }


    @Test
    public void read2() throws Exception {
        File file1 = new File("target/test/org.domain.Class", "2025.data");
        FileWriter fileWriter = new FileWriter(file1);
        fileWriter.write("type 1025 1\n{data1}");
        fileWriter.close();

        ServerConnection connection = new ServerConnection(IoUtils.asUtf8ByteStream("Rorg.domain.Class 2025"), out);
        server.process(connection);
        
        assertThat(out.toString(), is(equalTo(lineSeparated("ok\n{data1}\n"))));
    }

}
