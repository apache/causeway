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
package org.apache.causeway.viewer.wicket.model.models;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUploadModels {

    public ScalarConvertingModel<List<FileUpload>, Blob> blob(final @NonNull ScalarModel scalarModel) {
        return new ScalarConvertingModel<List<FileUpload>, Blob>(scalarModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected Blob toScalarValue(final @Nullable List<FileUpload> fileUploads) {

                if(fileUploads==null
                        || fileUploads.isEmpty()) {
                    return null;
                }

                final FileUpload fileUpload = fileUploads.get(0);
                final String contentType = fileUpload.getContentType();
                final String clientFileName = fileUpload.getClientFileName();
                final byte[] bytes = fileUpload.getBytes();
                final Blob blob = new Blob(clientFileName, contentType, bytes);
                return blob;
            }

            @Override
            protected List<FileUpload> fromScalarValue(final Blob blob) {
                return blob!=null
                        ? Collections.emptyList() //[CAUSEWAY-3203] just enough so we can distinguish the empty from the present case
                        : null;
            }

        };
    }

    public ScalarConvertingModel<List<FileUpload>, Clob> clob(
            final @NonNull ScalarModel scalarModel,
            final @NonNull Charset charset) {

        return new ScalarConvertingModel<List<FileUpload>, Clob>(scalarModel) {

            private static final long serialVersionUID = 1L;
            private final String charsetName = charset.name(); // Charset is not serializable

            @SneakyThrows
            @Override
            protected Clob toScalarValue(final @Nullable List<FileUpload> fileUploads) {

                if(fileUploads==null
                        || fileUploads.isEmpty()) {
                    return null;
                }

                final FileUpload fileUpload = fileUploads.get(0);
                final String contentType = fileUpload.getContentType();
                final String clientFileName = fileUpload.getClientFileName();
                final String str = new String(fileUpload.getBytes(), charsetName);
                final Clob clob = new Clob(clientFileName, contentType, str);
                return clob;
            }

            @Override
            protected List<FileUpload> fromScalarValue(final Clob clob) {
                return clob!=null
                        ? Collections.emptyList() //[CAUSEWAY-3203] just enough so we can distinguish the empty from the present case
                        : null;
            }

        };
    }

}
