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

package org.apache.isis.viewer.html.monitoring.systemconsole;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

public class FileConsole implements ServerConsole {
    final static Logger LOG = Logger.getLogger(FileConsole.class);
    private DataOutputStream dos;

    @Override
    public void close() {
    }

    @Override
    public void init(final Server server) {
    }

    @Override
    public void log() {
        log("");
    }

    @Override
    public void log(final String message) {
        try {
            LOG.info(message);
            dos = new DataOutputStream(new FileOutputStream("log.xxx"));
            dos.writeBytes(new Date() + " " + message + '\n');
            dos.close();
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
