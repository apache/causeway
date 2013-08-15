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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class DataFileWriter {

    // private static final Logger LOG = LoggerFactory.getLogger(DataWriter.class);

    private final List<FileContent> files;

    public DataFileWriter(final List<FileContent> files) {
        this.files = files;
    }

    public void writeData() throws IOException {
        for (final FileContent content : files) {
            if (Util.isDelete(content.command)) {
                final File f = Util.dataFile(content.type, content.id);
                f.delete();
            } else {
                writeFile(content);
            }
        }
    }

    // TODO to be consistent use PrintWriter
    private void writeFile(final FileContent content) throws IOException {
        FileOutputStream output = null;
        final File file = Util.dataFile(content.type, content.id);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }
        try {
            output = new FileOutputStream(file);
            content.write(output);
        } finally {
            Util.closeSafely(output);
        }
    }

    public void close() {
        // TODO Auto-generated method stub

    }

}
