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
package org.apache.isis.extensions.zip.dom.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.exceptions.UnrecoverableException;
import org.apache.isis.commons.internal.base._Bytes;

import lombok.Data;
import lombok.val;

@Service
public class ZipService {

    @Data
    public static class FileAndName {
        private final String name;
        private final File file;
    }

    /**
     * Rather than use the name of the file (which might be temporary files, for example)
     * we explicitly provide the name to use (in the ZipEntry).
     */
    public byte[] zipNamedFiles(final List<FileAndName> fileAndNameList) {

        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);

            for (val fileAndName : fileAndNameList) {
                zos.putNextEntry(new ZipEntry(fileAndName.getName()));

                try(val fis = new FileInputStream(fileAndName.getFile())){
                    zos.write(_Bytes.of(fis));
                }

                zos.closeEntry();
            }
            zos.close();
            return baos.toByteArray();
        } catch (final IOException ex) {
            throw new UnrecoverableException("Unable to create zip", ex);
        }
    }

    /**
     * As per {@link #zipNamedFiles(List)},
     * but using each file's name as the zip entry (rather than providing it).
     */
    public byte[] zipFiles(final List<File> fileList) {
        return zipNamedFiles(fileList.stream()
                           .map(file -> new FileAndName(file.getName(), file))
                           .collect(Collectors.toList())
                );
    }

    @Data
    public static class BytesAndName {
        private final String name;
        private final byte[] bytes;
    }

    /**
     * Similar to {@link #zipNamedFiles(List)}, but uses simple byte[] as the input, rather than files.
     *
     * @param bytesAndNameList
     */
    public byte[] zipNamedBytes(final List<BytesAndName> bytesAndNameList) {

        final byte[] bytes;
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);

            for (final BytesAndName ban : bytesAndNameList) {
                zos.putNextEntry(new ZipEntry(ban.getName()));
                zos.write(ban.getBytes());
                zos.closeEntry();
            }
            zos.close();
            bytes = baos.toByteArray();
        } catch (final IOException ex) {
            throw new UnrecoverableException("Unable to create zip", ex);
        }
        return bytes;
    }

}
