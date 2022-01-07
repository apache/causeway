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
package org.apache.isis.subdomains.zip.applib.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.exceptions.UnrecoverableException;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.subdomains.zip.applib.ZipService;

import lombok.val;

@Service
@Named("isis.sub.zip.ZipServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class ZipServiceDefault implements ZipService {

    @Override
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

    @Override
    public byte[] zipFiles(final List<File> fileList) {
        return zipNamedFiles(fileList.stream()
                           .map(file -> new FileAndName(file.getName(), file))
                           .collect(Collectors.toList())
                );
    }

    @Override
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
