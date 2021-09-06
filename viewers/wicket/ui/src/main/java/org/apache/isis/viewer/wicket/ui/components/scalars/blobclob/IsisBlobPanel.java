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
package org.apache.isis.viewer.wicket.ui.components.scalars.blobclob;


import java.util.List;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.IResource;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

/**
 * Panel for rendering scalars of type {@link org.apache.isis.applib.value.Blob Isis' applib.Blob}.
 */
public class IsisBlobPanel extends IsisBlobOrClobPanelAbstract<Blob> {

    private static final long serialVersionUID = 1L;

    public IsisBlobPanel(final String id, final ScalarModel model) {
        super(id, model);
    }


    @Override
    protected Blob getBlobOrClobFrom(final List<FileUpload> fileUploads) {
        final FileUpload fileUpload = fileUploads.get(0);
        final String contentType = fileUpload.getContentType();
        final String clientFileName = fileUpload.getClientFileName();
        final byte[] bytes = fileUpload.getBytes();
        final Blob blob = new Blob(clientFileName, contentType, bytes);
        return blob;
    }

    @Override
    protected IResource newResource(final Blob blob) {
        return new ByteArrayResource(blob.getMimeType().getBaseType(), blob.getBytes(), blob.getName());
    }



}