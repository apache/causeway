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
package org.apache.isis.subdomains.zip.applib;

import java.io.File;
import java.util.List;

import lombok.Data;

public interface ZipService {

    /**
     * Rather than use the name of the file (which might be temporary files, for example)
     * we explicitly provide the name to use (in the ZipEntry).
     */
    byte[] zipNamedFiles(List<FileAndName> fileAndNameList);

    /**
     * As per {@link #zipNamedFiles(List)},
     * but using each file's name as the zip entry (rather than providing it).
     */
    byte[] zipFiles(List<File> fileList);

    /**
     * Similar to {@link #zipNamedFiles(List)}, but uses simple byte[] as the input, rather than files.
     *
     * @param bytesAndNameList
     */
    byte[] zipNamedBytes(List<BytesAndName> bytesAndNameList);

    @Data
    public static class FileAndName {
        private final String name;
        private final File file;
    }

    @Data
    public static class BytesAndName {
        private final String name;
        private final byte[] bytes;
    }

}
