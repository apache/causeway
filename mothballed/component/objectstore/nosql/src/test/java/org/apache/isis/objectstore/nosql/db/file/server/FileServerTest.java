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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.isis.core.commons.lang.InputStreamExtensions;

public class FileServerTest {
    private FileServerProcessor server;
    private File logFile1;
    private File logFile2;
    private ByteArrayOutputStream out;

    @BeforeClass
    public static void setUp() throws Exception {
        Util.setDirectory("target/test", "services", "logs", "archive");
        Util.ensureDirectoryExists();
        new File("target/test/type").mkdir();
    }

    @Before
    public void startup() {
        logFile1 = recreateFile("target/test/logs", "recovery0.log");
        logFile2 = recreateFile("target/test/logs", "recovery1.log");

        final File dir = new File("target/test/org.domain.Class");
        dir.mkdirs();
        final File dir2 = new File("target/test/org.domain.Class2");
        dir2.mkdirs();

        server = new FileServerProcessor();
        server.startup();

        out = new ByteArrayOutputStream();
    }

    private static File recreateFile(final String parent, final String child) {
        final File file = new File(parent, child);
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
        final InputStream in = InputStreamExtensions.asUtf8ByteStream("R[org.domain.Class 1025]\n");
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ServerConnection connection = new ServerConnection(in, out);

        server.shutdown();
        server.process(connection);
        assertThat(new String(out.toByteArray(), "utf-8"), is(equalTo("abort\n")));
    }

    @Test
    public void writeAbortedAsDataNotComplete() throws Exception {
        final InputStream in = InputStreamExtensions.asUtf8ByteStream("W\nIorg.domain.Class 1025 null 1  \n{da");
        final ServerConnection connection = new ServerConnection(in, out);
        server.process(connection);

        assertThat(out.toString(), is(containsString("stream ended prematurely while reading data, aborting request")));
    }

    @Test
    public void writeAbortsIfMissingNextDataBlock() throws Exception {
        final InputStream in = InputStreamExtensions.asUtf8ByteStream("W\nIorg.domain.Class 1025 null 1  \n{data1}\n\n");
        final ServerConnection connection = new ServerConnection(in, out);
        server.process(connection);

        assertThat(out.toString(), is(containsString("stream ended prematurely while reading header, aborting request")));
    }

    @Test
    public void writeAbortedAsHeaderNotComplete() throws Exception {
        final InputStream in = InputStreamExtensions.asUtf8ByteStream("W\nIorg.domain.Class 1025");
        final ServerConnection connection = new ServerConnection(in, out);
        server.process(connection);

        assertThat(out.toString(), is(containsString("invalid header string, aborting request")));
    }

    @Test
    public void writeCreatesFilesUsingDataWriter() throws Exception {
        final File file1 = new File("target/test/org.domain.Class", "1025.data");
        final File file2 = new File("target/test/org.domain.Class", "1026.data");
        file1.delete();
        file2.delete();
        assertFalse(file1.exists());
        assertFalse(file2.exists());

        final InputStream in = InputStreamExtensions.asUtf8ByteStream("W\nIorg.domain.Class 1025 null 1  \n{data1}\n\nIorg.domain.Class 1026 null 1\n{data2}\n\n\n");
        final ServerConnection connection = new ServerConnection(in, out);
        server.process(connection);

        assertThat(out.toString(), is(equalTo("ok\n")));
        assertThat(file1, existsAndNotEmpty());
        assertThat(file2, existsAndNotEmpty());
    }

    @Test
    public void writeUpdatesFilesUsingDataWriter() throws Exception {
        final File file2 = new File("target/test/org.domain.Class2", "1026.data");
        final FileWriter fileWriter = new FileWriter(file2);
        final String originalData = "org.domain.Class 1026 21 {}";
        fileWriter.write(originalData);
        fileWriter.close();

        final ServerConnection connection = new ServerConnection(InputStreamExtensions.asUtf8ByteStream("W\nUorg.domain.Class2 1026 21 22 \n{data2}\n\n\n"), out);
        server.process(connection);

        assertThat(out.toString(), is(equalTo("ok\n")));
        assertThat(file2.length(), is(greaterThan((long) originalData.length())));
    }

    @Test
    public void writeUpdateFailsWhenVersionsDontMatch() throws Exception {
        final File file2 = new File("target/test/org.domain.Class", "1026.data");
        final FileWriter fileWriter = new FileWriter(file2);
        final String originalData = "org.domain.Class 1026 21\n{datax}\n\n\n***";
        fileWriter.write(originalData);
        fileWriter.close();

        final ServerConnection connection = new ServerConnection(InputStreamExtensions.asUtf8ByteStream("W\nUorg.domain.Class 1026 19 21 \n{data2}\n\n\n"), out);
        server.process(connection);

        assertThat(out.toString(), is(equalTo("error\nmismatch between FileContent version (19) and DataReader version (21)\n")));
    }

    @Test
    public void writeCreatesLogFile() throws Exception {
        final ServerConnection connection = new ServerConnection(InputStreamExtensions.asUtf8ByteStream("W\nIorg.domain.Class 1025 6 7\n{data1}\n\n\n"), out);
        server.process(connection);

        assertThat(out.toString(), is(equalTo("ok\n")));

        assertThat(logFile1, existsAndNotEmpty());
        assertThat(logFile2, not(existsAndNotEmpty()));
    }

    @Test
    public void readNonExistingFileThrowsException() throws Exception {
        final File file1 = new File("target/test/org.domain.Class", "2020.data");
        file1.delete();
        final ServerConnection connection = new ServerConnection(InputStreamExtensions.asUtf8ByteStream("Rorg.domain.Class 2020\n\n"), out);
        server.process(connection);

        final String string = out.toString();
        assertThat(string, startsWith("not-found"));
        assertThat(string, containsString("File not found for org.domain.Class/2020"));
    }

    @Test
    public void aTestTheTests() throws Exception {
        final File dir = new File("target/test/org.domain.Class");
        assertTrue(dir.exists());

        final File file1 = new File("target/test/org.domain.Class", "2025.data");
        assertTrue(file1.getParentFile().exists());

        final FileWriter fileWriter = new FileWriter(file1);
        assertNotNull(fileWriter);
        fileWriter.write("data");
        fileWriter.close();
    }

    @Test
    public void copyOfReadTest() throws Exception {
        final File file1 = new File("target/test/org.domain.Class2", "2025.data");
        final FileWriter fileWriter = new FileWriter(file1);
        fileWriter.write("type 1025 1\n{data1}");
        fileWriter.close();

        final ServerConnection connection = new ServerConnection(InputStreamExtensions.asUtf8ByteStream("Rorg.domain.Class2 2025\n\n"), out);
        server.process(connection);

        assertThat(out.toString(), is(equalTo("ok\n{data1}\n\n")));
    }

    @Test
    public void ReadFailIfEndsEarly() throws Exception {
        final ServerConnection connection = new ServerConnection(InputStreamExtensions.asUtf8ByteStream("Rorg.domain.Class 2010\n"), out);
        server.process(connection);

        assertThat(out.toString(), is(containsString("stream ended prematurely while reading end of command, aborting request")));
    }

    @Test
    public void lookReadRenamed() throws Exception {
        final File file1 = new File("target/test/org.domain.Class2", "2025.data");
        final FileWriter fileWriter = new FileWriter(file1);
        fileWriter.write("type 1025 1\n{data1}");
        fileWriter.close();

        final ServerConnection connection = new ServerConnection(InputStreamExtensions.asUtf8ByteStream("Rorg.domain.Class2 2025\n\n"), out);
        server.process(connection);

        assertThat(out.toString(), is(equalTo("ok\n{data1}\n\n")));
    }

    @Test
    public void read2() throws Exception {
        final File file1 = new File("target/test/org.domain.Class2", "2025.data");
        final FileWriter fileWriter = new FileWriter(file1);
        fileWriter.write("type 1025 1\n{data1}");
        fileWriter.close();

        final ServerConnection connection = new ServerConnection(InputStreamExtensions.asUtf8ByteStream("Rorg.domain.Class2 2025\n\n"), out);
        server.process(connection);

        assertThat(out.toString(), is(equalTo("ok\n{data1}\n\n")));
    }

    @Test
    public void hasNoInstances() throws Exception {
        final ServerConnection connection = new ServerConnection(InputStreamExtensions.asUtf8ByteStream("Iorg.domain.None\n\n"), out);
        server.process(connection);

        assertThat(out.toString(), is(equalTo("ok false\n")));
    }

    @Test
    public void hasInstances() throws Exception {
        final File file1 = new File("target/test/org.domain.Class2", "2025.data");
     //   file1.getParentFile().mkdirs();
        final FileWriter fileWriter = new FileWriter(file1);
        fileWriter.write("type 1025 1\n{data1}");
        fileWriter.close();

        final ServerConnection connection = new ServerConnection(InputStreamExtensions.asUtf8ByteStream("Iorg.domain.Class2\n\n"), out);
        server.process(connection);

        assertThat(out.toString(), is(equalTo("ok true\n")));
    }

}
