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

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.objectstore.nosql.NoSqlStoreException;

public class Util {

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private static final String DEFAULT_DIRECTORY = "data";
    private static final String SERVICES_DIRECTORY = "services";
    private static final String LOGS_DIRECTORY = "logs";
    private static final String LOGS_ARCHIVE_DIRECTORY = "archive";
    public static final String ABORT = "abort";
    public static final String OK = "ok";
    public static final String READ_ERROR = "Read error";
    public static final String FILE_NOT_FOUND = "File not found";
    private static final int NEWLINE = '\n';

    private static File dataDirectory = new File(DEFAULT_DIRECTORY);
    private static File serviceDirectory = new File(DEFAULT_DIRECTORY, SERVICES_DIRECTORY);
    private static File logDirectory = new File(DEFAULT_DIRECTORY, LOGS_DIRECTORY);
    private static File logArchiveDirectory = new File(DEFAULT_DIRECTORY, LOGS_ARCHIVE_DIRECTORY);

    static void setDirectory(final String data, final String services, final String logs, final String archive) {
        final String directory = data == null ? DEFAULT_DIRECTORY : data;
        Util.dataDirectory = new File(directory);
        Util.serviceDirectory = new File(directory, services == null ? SERVICES_DIRECTORY : services);
        Util.logDirectory = new File(directory, logs == null ? LOGS_DIRECTORY : logs);
        Util.logArchiveDirectory = new File(directory, archive == null ? LOGS_ARCHIVE_DIRECTORY : archive);
    }

    static void ensureDirectoryExists() {
        if (!serviceDirectory.exists()) {
            serviceDirectory.mkdirs();
        }
        if (!logDirectory.exists()) {
            logDirectory.mkdirs();
        }
        if (!logArchiveDirectory.exists()) {
            logArchiveDirectory.mkdirs();
        }
    }

    public static boolean isPopulated() {
        final FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                final String name = pathname.getName();
                return name.endsWith(".data") || name.endsWith(".id") || name.endsWith(".log");
            }
        };

        final File[] data = dataDirectory.listFiles();
        for (final File directory : data) {
            if (directory.isDirectory() && directory.listFiles(filter).length > 1) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasInstances(final String type) {
        final FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                final String name = pathname.getName();
                return name.endsWith(".data") || name.endsWith(".id") || name.endsWith(".log");
            }
        };

        final File[] data = dataDirectory.listFiles();
        for (final File directory : data) {
            if (directory.isDirectory() && directory.listFiles(filter).length > 1) {
                return true;
            }
        }
        return false;
    }

    public static String xxreadNext(final InputStream input) throws IOException {
        final byte[] buffer = new byte[100];
        int c;
        int i = 0;
        // TODO deal with buffer overrun
        while ((c = input.read()) != ' ' && c != '\n' && c != -1) {
            buffer[i++] = (byte) c;
        }
        if (i == 0) {
            throw new NoSqlStoreException("No data read from " + input);
        }
        final String read = new String(buffer, 0, i);
        LOG.debug("read " + read);
        return read;
    }

    public static byte[] xxreadData(final InputStream input) throws IOException {
        // TODO increase to suitable size
        byte[] buf = new byte[32];
        int i = 0;
        int c1 = input.read();
        if (c1 == '.') {
            return null;
        }
        int c2 = input.read();
        boolean isEnd;
        do {
            if (i == buf.length) {
                final byte[] newBuf = new byte[buf.length * 2];
                System.arraycopy(buf, 0, newBuf, 0, buf.length);
                buf = newBuf;
            }
            buf[i++] = (byte) c1;
            c1 = c2;
            c2 = input.read();
            isEnd = (c1 == NEWLINE && c2 == NEWLINE) || c2 == -1;
        } while (!isEnd);
        return buf;
    }

    static File directory(final String type) {
        return new File(dataDirectory, type);
    }

    static File dataFile(final String type, final String id) {
        final File dir = directory(type);
        return new File(dir, id + ".data");
    }

    public static File serviceFile(final String name) {
        return new File(serviceDirectory, name + ".id");
    }

    public static File serialNumberFile(final String name) {
        return new File(dataDirectory, "serialnumbers" + name.trim() + ".data");
    }

    static File logFile(final long id) {
        return new File(logDirectory, "recovery" + id + ".log");
    }

    static File tmpLogFile(final long id) {
        return new File(logDirectory, "recovery" + id + ".log.tmp");
    }

    public static File archiveLogFile(final long id) {
        return new File(logArchiveDirectory, "recovery" + id + ".log");
    }

    static LogRange logFileRange() {
        final LogRange logRange = new LogRange();
        final File[] listFiles = logDirectory.listFiles();
        if (listFiles != null) {
            for (final File file : listFiles) {
                final String name = file.getName();
                final String substring = name.substring(8, name.length() - 4);
                try {
                    final long sequence = Long.parseLong(substring);
                    logRange.add(sequence);
                } catch (final NumberFormatException ignore) {
                }
            }
        }
        return logRange;
    }

    static final char DELETE = 'D';

    public static final Charset ENCODING = Charset.forName("utf-8");

    public static boolean isDelete(final char command) {
        return command == Util.DELETE;
    }

    public static boolean isSave(final char command) {
        return command != Util.DELETE;
    }

    public static boolean shouldFileExist(final char command) {
        return command == 'D' || command == 'U';
    }

    public static InputStream trace(final InputStream inputStream, final boolean isOn) {
        return !isOn ? inputStream : new InputStream() {
            StringBuffer log = new StringBuffer();

            @Override
            public int read() throws IOException {
                final int b = inputStream.read();
                log(b);
                return b;
            }

            private void log(final int b) {
                log.append(b < 32 ? ("<" + b + ">" + (char) b) : (char) b);
                // System.out.print(b < 32 ? ("<" + b + ">" + (char) b) : (char)
                // b);
            }

            @Override
            public int read(final byte[] b) throws IOException {
                final int read = inputStream.read(b);
                for (int i = 0; i < read; i++) {
                    log(b[i]);
                }
                return read;
            }

            @Override
            public int read(final byte[] b, final int off, final int len) throws IOException {
                final int read = inputStream.read(b, off, len);
                for (int i = 0; i < read; i++) {
                    log(b[off + i]);
                }
                return read;
            }

            @Override
            public int available() throws IOException {
                return inputStream.available();
            }

            @Override
            public long skip(final long n) throws IOException {
                return inputStream.skip(n);
            }

            @Override
            public void close() throws IOException {
                // LOG.debug("in - " + log.toString());
                inputStream.close();
            }

            @Override
            public String toString() {
                return "in#" + Long.toHexString(hashCode()) + " " + log;
            }
        };
    }

    public static OutputStream trace(final OutputStream outputStream, final boolean isOn) {
        return !isOn ? outputStream : new OutputStream() {
            StringBuffer log = new StringBuffer();

            @Override
            public void write(final int b) throws IOException {
                log(b);
                outputStream.write(b);
            }

            private void log(final int b) {
                log.append(b < 32 ? ("<" + b + ">" + (char) b) : (char) b);
                // System.out.print(b < 32 ? ("<" + b + ">" + (char) b) : (char)
                // b);
            }

            @Override
            public void write(final byte[] b) throws IOException {
                for (final byte element : b) {
                    log(element);
                }
                outputStream.write(b);
            }

            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                for (int i = 0; i < len; i++) {
                    log(b[off + i]);
                }
                outputStream.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                outputStream.flush();
            }

            @Override
            public void close() throws IOException {
                // LOG.debug("out - " + log.toString());
                outputStream.close();
            }

            @Override
            public String toString() {
                return "out#" + Long.toHexString(hashCode()) + " " + log;
            }
        };
    }

    public static void closeSafely(final FileOutputStream output) {
        if (output != null) {
            try {
                output.flush();
                output.close();
            } catch (final IOException e) {
                // throw new ObjectAdapterRuntimeException(e);
            }
        }
    }

}
