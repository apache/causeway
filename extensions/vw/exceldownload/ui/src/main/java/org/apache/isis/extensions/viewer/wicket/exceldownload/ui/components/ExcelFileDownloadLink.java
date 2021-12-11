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
package org.apache.isis.extensions.viewer.wicket.exceldownload.ui.components;

import java.io.File;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;

class ExcelFileDownloadLink extends DownloadLink {

    private static final long serialVersionUID = 1L;

    private final String xlsxFileName;

    public ExcelFileDownloadLink(
            final String id, final LoadableDetachableModel<File> model, final String xlsxFileName) {
        super(id, model, xlsxFileName);
        this.xlsxFileName = xlsxFileName;
    }

    @Override
    public void onClick() {

        EntityPage.jaxbViewmodelRefresh(getPage());

        final File file = getModelObject();
        if (file == null)
        {
            throw new IllegalStateException(getClass().getName() +
                    " failed to retrieve a File object from model");
        }

        String fileName = encodedFileName();

        final IResourceStream resourceStream = new FileResourceStream(
                new org.apache.wicket.util.file.File(file)) {

            private static final long serialVersionUID = 1L;

            @Override
            public String getContentType() {
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml";
            }
        };

        getRequestCycle().scheduleRequestHandlerAfterCurrent(
                new ResourceStreamRequestHandler(resourceStream)
                {
                    @Override
                    public void respond(final IRequestCycle requestCycle)
                    {
                        super.respond(requestCycle);
                        Files.remove(file);
                    }
                }.setFileName(fileName)
                .setContentDisposition(ContentDisposition.ATTACHMENT));
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);
        tag.put("download", encodedFileName());
    }

    private String encodedFileName() {
        return encoded(this.xlsxFileName);
    }

    private String encoded(final String xlsxFileName) {
        return UrlEncoder.QUERY_INSTANCE.encode(xlsxFileName, getRequest().getCharset());
    }

}