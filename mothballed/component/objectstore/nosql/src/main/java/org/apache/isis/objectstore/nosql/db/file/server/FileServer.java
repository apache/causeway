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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.lang.ObjectExtensions;
import org.apache.isis.objectstore.nosql.NoSqlStoreException;

public class FileServer {

    private static final Logger LOG = LoggerFactory.getLogger(FileServer.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_SERVICE_PORT = 9100;
    private static final int DEFAULT_CONTROL_PORT = 9101;
    private static final int DEFAULT_SYNC_PORT = 9102;
    private static final int BACKLOG = 0;
    private static final int INIT = 1;
    private static final int RECOVERY_LOG = 2;

    public static void main(final String[] args) throws IOException, ParseException {

        final Options options = new Options();
        options.addOption("h", "help", false, "Show this help");
        options.addOption("m", "mode", true, "mode: normal | secondary | recovery | archive");

        final CommandLineParser parser = new BasicParser();
        final CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption('h')) {
            printHelp(options);
            return;
        }

        final String mode = cmd.getOptionValue("m");

        final List<String> argList = ObjectExtensions.asT(cmd.getArgList());
        if ("recovery".equals(mode)) {
            final FileServer fileServer = new FileServer();
            fileServer.startRecovery(argList);
        } else if ("archive".equals(mode)) {
            final FileServer fileServer = new FileServer();
            fileServer.startArchive(argList);
        } else if ("secondary".equals(mode)) {
            final FileServer fileServer = new FileServer();
            fileServer.startSecondary();
        } else if (mode == null || "normal".equals(mode)) {
            final FileServer fileServer = new FileServer();
            fileServer.startNormal();
        } else {
            printHelp(options);
        }
    }

    private static void printHelp(final Options options) {
        final HelpFormatter help = new HelpFormatter();
        help.printHelp("FileSever [OPTIONS] [FIRST RECOVERY FILES] [LAST RECOVERY FILES]", options);
    }

    private FileServerProcessor server;
    private CompositeConfiguration config;

    private boolean awaitConnections = true;
    private boolean isQuiescent = false;
    private long requests;

    public FileServer() {
        org.apache.log4j.PropertyConfigurator.configure("config/logging.properties");

        try {
            config = new CompositeConfiguration();
            config.addConfiguration(new SystemConfiguration());
            config.addConfiguration(new PropertiesConfiguration("config/server.properties"));

            final String data = config.getString("fileserver.data");
            final String services = config.getString("fileserver.services");
            final String logs = config.getString("fileserver.logs");
            final String archive = config.getString("fileserver.archive");

            Util.setDirectory(data, services, logs, archive);
            server = new FileServerProcessor();
        } catch (final ConfigurationException e) {
            LOG.error("configuration failure", e);
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    private void startNormal() {
        new Thread("control") {
            @Override
            public void run() {
                startControl();
            };
        }.start();
        new Thread("service") {
            @Override
            public void run() {
                startService();
            };
        }.start();
        new Thread("log-rolling") {
            @Override
            public void run() {
                startLogRolling();
            }
        }.start();
        if (config.getBoolean("fileserver.sync", false)) {
            new Thread("sync") {
                @Override
                public void run() {
                    startSyncing();
                };
            }.start();
        } else {
            LOG.info("not syncing to secondary server");
        }

    }

    private void startService() {
        final String serviceHost = config.getString("fileserver.host", DEFAULT_HOST);
        final int servicePort = config.getInt("fileserver.port", DEFAULT_SERVICE_PORT);
        final int connectionTimeout = config.getInt("fileserver.connection.timeout", 5000);
        final int readTimeout = config.getInt("fileserver.read.timeout", 5000);

        ServerSocket socket = null;
        try {
            LOG.debug("setting up service socket on " + serviceHost + ":" + servicePort);
            final InetAddress address = InetAddress.getByName(serviceHost);
            socket = new ServerSocket(servicePort, BACKLOG, address);
            socket.setSoTimeout(connectionTimeout);
            LOG.info("file service listenting on " + socket.getInetAddress().getHostAddress() + " port " + socket.getLocalPort());
            LOG.debug("file service listenting on " + socket);
            final LogRange logFileRange = Util.logFileRange();
            if (!logFileRange.noLogFile()) {
                final long lastRecoveryFile = logFileRange.getLast();
                final File file = Util.logFile(lastRecoveryFile);
                LOG.info("replaying last recovery file: " + file.getAbsolutePath());
                recover(file);
            }
            server.startup();
        } catch (final UnknownHostException e) {
            LOG.error("Unknown host " + serviceHost, e);
            System.exit(0);
        } catch (final IOException e) {
            LOG.error("start failure - networking not set up for " + serviceHost, e);
            System.exit(0);
        } catch (final RuntimeException e) {
            LOG.error("start failure", e);
            System.exit(0);
        }
        do {
            try {
                while (isQuiescent) {
                    try {
                        Thread.sleep(300);
                    } catch (final InterruptedException ignore) {
                    }
                }
                final Socket connection = socket.accept();
                LOG.debug("connection from " + connection);
                connection.setSoTimeout(readTimeout);
                serviceConnection(connection, readTimeout);
            } catch (final SocketTimeoutException expected) {
            } catch (final IOException e) {
                LOG.error("networking problem", e);
            }
        } while (awaitConnections);
    }

    private void serviceConnection(final Socket connection, final int readTimeout) {
        try {
            final InputStream input = connection.getInputStream();
            final OutputStream output = connection.getOutputStream();
            final ServerConnection pipe = new ServerConnection(input, output);
            requests++;
            server.process(pipe);
            pipe.logComplete();
        } catch (final NoSqlStoreException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                LOG.error("read timed out after " + (readTimeout / 1000.0) + " seconds", e);
            } else {
                LOG.error("file server failure", e);
            }
        } catch (final IOException e) {
            LOG.error("networking failure", e);
        } catch (final RuntimeException e) {
            LOG.error("request failure", e);
        } finally {
            try {
                connection.close();
            } catch (final IOException e) {
                LOG.warn("failure to close connection", e);
            }
        }
    }

    private void startSyncing() {
        final String syncHost = config.getString("fileserver.sync-host", DEFAULT_HOST);
        final int syncPort = config.getInt("fileserver.sync-port", DEFAULT_SYNC_PORT);
        final int connectionTimeout = config.getInt("fileserver.connection.timeout", 5000);

        LOG.info("preparing to sync to secondary server on " + syncHost + " port " + syncPort);

        final InetAddress address;
        try {
            address = InetAddress.getByName(syncHost);
        } catch (final UnknownHostException e) {
            LOG.error("Unknown host " + syncHost, e);
            System.exit(0);
            return;
        }

        while (awaitConnections) {
            Socket socket = null;
            try {
                socket = new Socket(address, syncPort);
                LOG.info("sync connected to " + socket.getInetAddress().getHostAddress() + " port " + socket.getLocalPort());

                final CRC32 crc32 = new CRC32();
                final DataOutput output = new DataOutputStream(new CheckedOutputStream(socket.getOutputStream(), crc32));
                final DataInput input = new DataInputStream(socket.getInputStream());
                output.writeByte(INIT);
                long logId = input.readLong();
                do {
                    final long nextLogId = logId + 1;
                    final File file = Util.logFile(nextLogId);
                    if (file.exists() && server.getLogger().isWritten(nextLogId)) {
                        logId++;

                        output.writeByte(RECOVERY_LOG);
                        crc32.reset();
                        output.writeLong(logId);

                        LOG.info("sending recovery file: " + file.getName());
                        final BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(file));

                        final byte[] buffer = new byte[8092];
                        int read;
                        while ((read = fileInput.read(buffer)) > 0) {
                            output.writeInt(read);
                            output.write(buffer, 0, read);
                        }
                        output.writeInt(0);

                        output.writeLong(crc32.getValue());
                    }
                    try {
                        Thread.sleep(300);
                    } catch (final InterruptedException ignore) {
                    }

                    while (isQuiescent) {
                        try {
                            Thread.sleep(300);
                        } catch (final InterruptedException ignore) {
                        }
                    }
                } while (awaitConnections);

            } catch (final ConnectException e) {
                LOG.warn("not yet connected to secondary server at " + syncHost + " port " + syncPort);
                try {
                    Thread.sleep(connectionTimeout);
                } catch (final InterruptedException ignore) {
                }
            } catch (final IOException e) {
                LOG.error("start failure - networking not set up for " + syncHost, e);
                try {
                    Thread.sleep(300);
                } catch (final InterruptedException ignore) {
                }
            } catch (final RuntimeException e) {
                LOG.error("start failure", e);
                try {
                    Thread.sleep(300);
                } catch (final InterruptedException ignore) {
                }
            }
        }

    }

    private void startControl() {
        final String controlHost = config.getString("fileserver.control-host", DEFAULT_HOST);
        final int controlPort = config.getInt("fileserver.control-port", DEFAULT_CONTROL_PORT);
        final int connectionTimeout = config.getInt("fileserver.connection.timeout", 5000);

        ServerSocket socket = null;
        try {
            LOG.debug("setting up control socket on " + controlHost + ":" + controlPort);
            final InetAddress address = InetAddress.getByName(controlHost);
            socket = new ServerSocket(controlPort, 0, address);
            socket.setSoTimeout(connectionTimeout);
            LOG.info("file control listenting on " + socket.getInetAddress().getHostAddress() + " port " + socket.getLocalPort());
            LOG.debug("file control listenting on " + socket);
        } catch (final UnknownHostException e) {
            LOG.error("Unknown host " + controlHost, e);
            System.exit(0);
        } catch (final IOException e) {
            LOG.error("start failure - networking not set up for " + controlHost, e);
            System.exit(0);
        } catch (final RuntimeException e) {
            LOG.error("start failure", e);
            System.exit(0);
        }
        do {
            try {
                final Socket connection = socket.accept();
                LOG.info("control connection from " + connection);
                controlConnection(connection);
            } catch (final SocketTimeoutException expected) {
            } catch (final IOException e) {
                LOG.error("networking problem", e);
            }
        } while (awaitConnections);
    }

    private void controlConnection(final Socket connection) {
        try {
            final InputStream input = connection.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            final OutputStream output = connection.getOutputStream();
            final PrintWriter print = new PrintWriter(output);
            print.print("> ");
            print.flush();
            String line;
            while ((line = reader.readLine()) != null) {
                if ("shutdown".equals(line)) {
                    awaitConnections = false;
                    print.println("Server shutdown initiated...");
                    print.flush();
                    server.shutdown();
                    break;
                } else if ("quiesce".equals(line)) {
                    isQuiescent = true;
                    final String message = "Placing server in a quiescent state";
                    LOG.info(message);
                    print.println(message);
                    print.print("> ");
                    print.flush();
                } else if ("resume".equals(line)) {
                    if (isQuiescent) {
                        isQuiescent = false;
                        final String message = "Resuming from a quiescent state";
                        LOG.info(message);
                        print.println(message);
                    } else {
                        print.println("Can't resume as not currently in a quiescent state");
                    }
                    print.print("> ");
                    print.flush();
                } else if ("quit".equals(line)) {
                    print.println("Bye");
                    print.flush();
                    break;
                } else if ("status".equals(line)) {
                    print.println("requests: " + requests);
                    print.println("quiescent: " + isQuiescent);
                    print.print("> ");
                    print.flush();
                } else {
                    print.println("Unknown command, valid commands are: quit, quiesce, status, resume, shutdown");
                    print.print("> ");
                    print.flush();
                }
            }
        } catch (final IOException e) {
            LOG.error("networking failure", e);
        } catch (final RuntimeException e) {
            LOG.error("request failure", e);
        } finally {
            try {
                connection.close();
            } catch (final IOException e) {
                LOG.warn("failure to close connection", e);
            }
        }
    }

    private void startRecovery(final List<String> list) {
        LOG.info("starting recovery");
        final LogRange logFileRange = Util.logFileRange();
        if (logFileRange.noLogFile()) {
            System.err.println("No recovery files found");
            System.exit(0);
        }
        final long lastId = logFileRange.getLast();
        LOG.info("last log file is " + Util.logFile(lastId).getName());

        long startId = lastId;
        long endId = lastId;

        final int size = list.size();
        if (size > 0) {
            startId = Long.valueOf(list.get(0));
            if (size > 1) {
                endId = Long.valueOf(list.get(1));
            }
        }
        if (startId < logFileRange.getFirst() || startId > lastId || endId > lastId) {
            System.err.println("File IDs invalid: they must be between " + logFileRange.getFirst() + " and " + lastId);
            System.exit(0);
        }
        if (startId > endId) {
            System.err.println("File IDs invalid: start must be before the end");
            System.exit(0);
        }

        Util.ensureDirectoryExists();
        for (long id = startId; id <= endId; id++) {
            final File file = Util.logFile(id);
            LOG.info("recovering data from " + file.getName());
            recover(file);
        }
        LOG.info("recovery complete");
    }

    private void startArchive(final List<String> list) {
        LOG.info("starting archiving");
        final LogRange logFileRange = Util.logFileRange();
        if (logFileRange.noLogFile()) {
            System.err.println("No recovery files found");
            System.exit(0);
        }
        final long lastId = logFileRange.getLast();
        LOG.info("last log file is " + Util.logFile(lastId).getName());

        long endId = lastId - 1;

        final int size = list.size();
        if (size > 0) {
            endId = Long.valueOf((String) list.get(0));
        }
        if (endId >= lastId) {
            System.err.println("File ID invalid: they must be less that " + lastId);
            System.exit(0);
        }
        final long startId = logFileRange.getFirst();
        for (long id = startId; id <= endId; id++) {
            final File file = Util.logFile(id);
            LOG.info("moving " + file.getName());
            final File destination = Util.archiveLogFile(id);
            file.renameTo(destination);
        }
        LOG.info("archive complete");

    }

    private void startSecondary() {
        final String serviceHost = config.getString("fileserver.sync-host", DEFAULT_HOST);
        final int servicePort = config.getInt("fileserver.sync-port", DEFAULT_SYNC_PORT);

        Util.ensureDirectoryExists();
        ServerSocket socket = null;
        try {
            LOG.debug("setting up syncing socket on " + serviceHost + ":" + servicePort);
            final InetAddress address = InetAddress.getByName(serviceHost);
            socket = new ServerSocket(servicePort, 0, address);
            LOG.info("listenting on " + socket.getInetAddress().getHostAddress() + " port " + socket.getLocalPort());
            LOG.debug("listenting on " + socket);
            do {
                syncConnection(socket.accept(), 0);
            } while (awaitConnections);
        } catch (final UnknownHostException e) {
            LOG.error("Unknown host " + serviceHost, e);
            System.exit(0);
        } catch (final IOException e) {
            LOG.error("start failure - networking not set up for " + serviceHost, e);
            System.exit(0);
        } catch (final RuntimeException e) {
            LOG.error("start failure", e);
            System.exit(0);
        }
    }

    private void syncConnection(final Socket connection, final int readTimeout) {
        try {
            final CRC32 crc32 = new CRC32();
            final DataOutput output = new DataOutputStream(connection.getOutputStream());
            final DataInput input = new DataInputStream(new CheckedInputStream(connection.getInputStream(), crc32));

            if (input.readByte() != INIT) {
                return;
            }

            final LogRange logFileRange = Util.logFileRange();
            final long lastId = logFileRange.noLogFile() ? -1 : logFileRange.getLast();
            output.writeLong(lastId);
            do {
                if (input.readByte() != RECOVERY_LOG) {
                    return;
                }
                crc32.reset();
                final long logId = input.readLong();
                final File file = Util.tmpLogFile(logId);
                LOG.info("syncing recovery file: " + file.getName());
                final BufferedOutputStream fileOutput = new BufferedOutputStream(new FileOutputStream(file));

                final byte[] buffer = new byte[8092];
                int length;
                while ((length = input.readInt()) > 0) {
                    input.readFully(buffer, 0, length);
                    fileOutput.write(buffer, 0, length);
                }
                fileOutput.close();

                final long calculatedChecksum = crc32.getValue();
                final long sentChecksum = input.readLong();
                if (calculatedChecksum != sentChecksum) {
                    throw new NoSqlStoreException("Checksum didn't match during download of " + file.getName());
                }

                recover(file);
                final File renameTo = Util.logFile(logId);
                file.renameTo(renameTo);
            } while (true);
        } catch (final NoSqlStoreException e) {
            LOG.error("file server failure", e);
        } catch (final IOException e) {
            LOG.error("networking failure", e);
        } catch (final RuntimeException e) {
            LOG.error("request failure", e);
        } finally {
            try {
                connection.close();
            } catch (final IOException e) {
                LOG.warn("failure to close connection", e);
            }
        }

        // TODO restart
    }

    private void recover(final File file) {
        LineNumberReader reader = null;
        try {
            reader = new LineNumberReader(new InputStreamReader(new FileInputStream(file), Util.ENCODING));

            while (true) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (!line.startsWith("#transaction started")) {
                    throw new NoSqlStoreException("No transaction start found: " + line + " (" + reader.getLineNumber() + ")");
                }
                readTransaction(reader);
            }
        } catch (final IOException e) {
            throw new NoSqlStoreException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    throw new NoSqlStoreException(e);
                }
            }
        }
    }

    private void readTransaction(final LineNumberReader reader) throws IOException {
        final ArrayList<FileContent> files = new ArrayList<FileContent>();
        final DataFileWriter content = new DataFileWriter(files);
        String header;
        while ((header = reader.readLine()) != null) {
            if (header.startsWith("#transaction ended")) {
                LOG.debug("transaction read in (ending " + reader.getLineNumber() + ")");
                content.writeData();
                reader.readLine();
                return;
            }
            if (header.startsWith("S")) {
                final String[] split = header.substring(1).split(" ");
                final String key = split[0];
                final String name = split[1];
                server.saveService(key, name);
                reader.readLine();
            } else if (header.startsWith("B")) {
                final String[] split = header.substring(1).split(" ");
                final String name = split[0];
                final long nextBatch = Long.valueOf(split[1]);
                server.saveNextBatch(name, nextBatch);
                reader.readLine();
            } else {
                FileContent elementData;
                elementData = readElementData(header, reader);
                files.add(elementData);
            }
        }
        LOG.warn("transaction has no ending marker so is incomplete and will not be restored (ending " + reader.getLineNumber() + ")");
    }

    private FileContent readElementData(final String header, final LineNumberReader reader) throws IOException {
        final StringBuffer content = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0) {
                break;
            }
            content.append(line);
            content.append('\n');
        }

        final char command = header.charAt(0);
        final String[] split = header.substring(1).split(" ");
        final String type = split[0];
        final String id = split[1];
        final String version = split[2];
        return new FileContent(command, id, null, version, type, content.toString());
    }

    private void startLogRolling() {
        final int rollPeriod = config.getInt("fileserver.log-period", 5);
        final long sleepTime = rollPeriod * 60 * 1000;

        while (awaitConnections) {
            final LogWriter logger = server.getLogger();
            if (logger != null) {
                logger.startNewFile();
            }
            try {
                Thread.sleep(sleepTime);
            } catch (final InterruptedException ignore) {
            }
        }
    }
}
