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

package org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib;


import java.nio.charset.Charset;
import java.util.List;

import com.google.common.base.Charsets;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.request.resource.CharSequenceResource;
import org.apache.wicket.request.resource.IResource;

import org.apache.isis.applib.value.Clob;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

/**
 * Panel for rendering scalars of type {@link Clob Isis' applib.Clob}.
 *
 * <p>
 *    TODO: for now, this only handles CLOBs encoded as UTF-8.  One option might be to 'guess' the character encoding, eg akin to cpdetector?
 * </p>
 */
public class IsisClobPanel extends IsisBlobOrClobPanelAbstract<Clob> {

    private static final long serialVersionUID = 1L;

    private static final Charset CHARSET = Charsets.UTF_8;

    public IsisClobPanel(final String id, final ScalarModel model) {
        super(id, model);
    }

    @Override
    protected Clob getBlobOrClobFrom(final List<FileUpload> fileUploads) {
        final FileUpload fileUpload = fileUploads.get(0);
        final String contentType = fileUpload.getContentType();
        final String clientFileName = fileUpload.getClientFileName();
        final String str = new String(fileUpload.getBytes(), CHARSET);
        final Clob blob = new Clob(clientFileName, contentType, str);
        return blob;
    }

    @Override
    protected IResource newResource(final Clob clob) {
        return new CharSequenceResource(clob.getMimeType().getBaseType(), clob.getChars(), clob.getName());
    }

}