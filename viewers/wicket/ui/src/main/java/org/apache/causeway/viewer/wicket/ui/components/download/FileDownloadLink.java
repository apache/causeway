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
package org.apache.causeway.viewer.wicket.ui.components.download;

import java.io.File;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.encoding.UrlEncoder;

import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

public class FileDownloadLink extends DownloadLink {

    private static final long serialVersionUID = 1L;

    private final CommonMimeType mime;
    private final String fileName;

    public FileDownloadLink(
            final String id, final CommonMimeType mime, final String fileName,
            final IModel<File> model) {
        super(id, model, fileName);
        this.mime = mime;
        this.fileName = fileName;
    }

    @Override
    public void onClick() {
        // lazily creates a temporary file on the host, to be consumed for download and then deleted by this handler
        Wkt.fileDownloadClickHandler(getModel(), mime, fileName);
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);
        var encodedFileName =
                UrlEncoder.QUERY_INSTANCE.encode(fileName, getRequest().getCharset());
        tag.put("download", encodedFileName);
    }

}
