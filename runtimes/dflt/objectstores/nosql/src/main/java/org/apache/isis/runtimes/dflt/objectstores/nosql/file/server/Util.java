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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.apache.isis.runtimes.dflt.objectstores.nosql.NoSqlStoreException;


public class Util {
    
    private static final Logger LOG = Logger.getLogger(Util.class);
    
    private static final String DEFAULT_DIRECTORY = "data";
    private static final String SERVICES_DIRECTORY = "services";
    private static final String LOGS_DIRECTORY = "logs";
    public static final String ABORT = "abort";
    public static final String OK = "ok";
    public static final String READ_ERROR = "Read error";
    public static final String FILE_NOT_FOUND = "File not found";
    private static final int NEWLINE = (int) '\n';

    private static File dataDirectory = new File(DEFAULT_DIRECTORY);
    private static File serviceDirectory = new File(DEFAULT_DIRECTORY, SERVICES_DIRECTORY);
    private static File logDirectory = new File(DEFAULT_DIRECTORY, LOGS_DIRECTORY);

    static void setDirectory(String data, String services, String logs) {
        String directory = data == null ? DEFAULT_DIRECTORY : data;
        Util.dataDirectory = new File(directory);
        Util.serviceDirectory = new File(directory, services == null ? SERVICES_DIRECTORY : services);
        Util.logDirectory = new File(directory, logs == null ? LOGS_DIRECTORY : logs);
    }

    static void ensureDirectoryExists() {
        if (!serviceDirectory.exists()) {
            serviceDirectory.mkdirs();
        }
        if (!logDirectory.exists()) {
            logDirectory.mkdirs();
        }
    }
    
    public static boolean isPopulated() {
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                String name = pathname.getName();
                return name.endsWith(".data") || name.endsWith(".id") || name.endsWith(".log");
            }
        };

        File[] data = dataDirectory.listFiles();
        for (File directory : data) {
            if (directory.isDirectory() && directory.listFiles(filter).length > 1) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasInstances(String type) {
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                String name = pathname.getName();
                return name.endsWith(".data") || name.endsWith(".id") || name.endsWith(".log");
            }
        };

        File[] data = dataDirectory.listFiles();
        for (File directory : data) {
            if (directory.isDirectory() && directory.listFiles(filter).length > 1) {
                return true;
            }
        }
        return false;
    }

     
    public static String xxreadNext(InputStream input) throws IOException {
        byte[] buffer = new byte[100];
        int c;
        int i = 0;
        // TODO deal with buffer overrun
        while ((c = input.read()) != ' ' && c != '\n' && c != -1) {
            buffer[i++] = (byte) c;
        }
        if (i == 0) {
            throw new NoSqlStoreException("No data read from " + input);
        }
        String read = new String(buffer, 0, i);
        LOG.debug("read " + read);
        return read;
    }
    
    public static byte[] xxreadData(InputStream input) throws IOException {
        // TODO increase to suitable size
        byte[] buf = new byte[32];
        int i = 0;
        int c1 =  input.read();
        if (c1 == '.') {
            return null;
        }
        int c2 =  input.read();
        boolean isEnd;
        do {
            if (i == buf.length) {
                byte[] newBuf = new byte[buf.length * 2];
                System.arraycopy(buf, 0, newBuf, 0, buf.length);
                buf = newBuf;
            }
            buf[i++] = (byte) c1;
            c1 = c2;
            c2 =  input.read();
            isEnd = (c1 == NEWLINE && c2 == NEWLINE) || c2 == -1;
        } while(!isEnd);
        return buf;
    }

    static File directory(String type) {
        return new File(dataDirectory, type);
    }

    static File dataFile(String type, String id) {
        File dir = directory(type);
        return new File(dir, id + ".data");
    }

    public static File serviceFile(String name) {
        return new File(serviceDirectory , name + ".id");
    }

    public static File serialNumberFile(String name) {
        return new File(dataDirectory , "serialnumbers" + name.trim() + ".data");
    }

    static File logFile(int id) {
        return new File(logDirectory , "recovery" + id + ".log");
    }

    static final char DELETE = 'D';

    public static final Charset ENCODING = Charset.forName("utf-8");

    public static boolean isDelete(char command) {
        return command == Util.DELETE;
    }

    public static boolean isSave(char command) {
        return command != Util.DELETE;
    }

    public static boolean shouldFileExist(char command) {
        return command == 'D' || command == 'U';
    }

    

    public static InputStream trace(final InputStream inputStream, boolean isOn) {
        return !isOn ? inputStream : new InputStream() {
            StringBuffer log = new StringBuffer();

            public int read() throws IOException {
                int b = inputStream.read();
                log(b);
                return b;
            }

            private void log(int b) {
                log.append(b < 32 ? ("<" + b + ">" + (char) b) : (char) b);
                // System.out.print(b < 32 ? ("<" + b + ">" + (char) b) : (char) b);
            }
            
            public int read(byte[] b) throws IOException {
                int read = inputStream.read(b);
                for (int i = 0; i < read; i++) {
                    log(b[i]);
                }
                return read;
            }
            
            public int read(byte[] b, int off, int len) throws IOException {
                int read = inputStream.read(b, off, len);
                for (int i = 0; i < read; i++) {
                    log(b[off + i]);
                }
                return read;
            }
            
            public int available() throws IOException {
                return inputStream.available();
            }
            
            public long skip(long n) throws IOException {
                return inputStream.skip(n);
            }
            
            public void close() throws IOException {
                // LOG.debug("in - " + log.toString());
                inputStream.close();
            }
            
            public String toString() {
                return "in#" + Long.toHexString(hashCode()) + " " + log;
            }
        };
    }

    public static OutputStream trace(final OutputStream outputStream, boolean isOn) {
        return !isOn ? outputStream : new OutputStream() {
            StringBuffer log = new StringBuffer();

            public void write(int b) throws IOException {
                log(b);
                outputStream.write(b);
            }
            
            private void log(int b) {
                log.append(b < 32 ? ("<" + b + ">" + (char) b) : (char) b);
                // System.out.print(b < 32 ? ("<" + b + ">" + (char) b) : (char) b);
            }
            
            public void write(byte[] b) throws IOException {
                for (int i = 0; i < b.length; i++) {
                    log(b[i]);
                }
                outputStream.write(b);
            }
            
            public void write(byte[] b, int off, int len) throws IOException {
                for (int i = 0; i < len; i++) {
                    log(b[off + i]);
                }
                outputStream.write(b, off, len);
            }
            
            public void flush() throws IOException {
                outputStream.flush();
            }
            
            public void close() throws IOException {
                // LOG.debug("out - " + log.toString());
                outputStream.close();
            }
            
            public String toString() {
                return "out#" + Long.toHexString(hashCode()) + " " + log;
            }
        };
    }

}

