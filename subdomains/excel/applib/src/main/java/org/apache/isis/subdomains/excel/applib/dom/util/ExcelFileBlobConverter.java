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
package org.apache.isis.subdomains.excel.applib.dom.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.poi.util.IOUtils;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.subdomains.excel.applib.dom.ExcelService;

import lombok.val;

/**
 * @since 2.0 {@index}
 */
public class ExcelFileBlobConverter {

    public Blob toBlob(final String name, final File file) {
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        try {
            fis = new FileInputStream(file);
            baos = new ByteArrayOutputStream();
            IOUtils.copy(fis, baos);
            return new Blob(name, ExcelService.XSLX_MIME_TYPE, baos.toByteArray());
        } catch (IOException ex) {
            throw new ExcelService.Exception(ex);
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(baos);
        }
    }

    public Blob toBlob(final String name, final URL resource) {
        byte[] bytes = getBytes(resource);
        return new Blob("unused", ExcelService.XSLX_MIME_TYPE, bytes);
    }

    // -- bytes

    private byte[] bytes;

    private byte[] getBytes(URL resource) {
        if (bytes == null) {
                bytes = readBytes(resource);
        }
        return bytes;
    }

    private byte[] readBytes(URL resource) {
        try(val is = resource.openStream()) {
            return _Bytes.of(is);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read from resource: " + resource);
        }
    }



}
