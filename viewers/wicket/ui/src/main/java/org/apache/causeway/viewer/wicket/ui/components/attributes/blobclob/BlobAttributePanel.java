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
package org.apache.causeway.viewer.wicket.ui.components.attributes.blobclob;

import java.util.List;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.IResource;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.viewer.wicket.model.models.FileUploadModels;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;

/**
 * Panel for rendering scalars of type {@link org.apache.causeway.applib.value.Blob Causeway' applib.Blob}.
 */
class BlobAttributePanel extends BlobOrClobAttributePanel<Blob> {

    private static final long serialVersionUID = 1L;

    public BlobAttributePanel(final String id, final UiAttributeWkt model) {
        super(id, model, Blob.class);
    }

    @Override
    protected IModel<List<FileUpload>> fileUploadModel() {
        return FileUploadModels.blob(attributeModel());
    }

    @Override
    protected IResource newResource(final Blob blob) {
        return new ByteArrayResource(blob.mimeType().getBaseType(), blob.bytes(), blob.name());
    }

}
