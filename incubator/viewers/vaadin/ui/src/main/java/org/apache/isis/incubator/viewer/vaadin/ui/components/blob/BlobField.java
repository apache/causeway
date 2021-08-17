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
package org.apache.isis.incubator.viewer.vaadin.ui.components.blob;

import java.io.ByteArrayInputStream;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;

import org.apache.isis.applib.value.Blob;

import lombok.val;

public class BlobField extends CustomField<Blob> {

    private static final long serialVersionUID = 1L;

    private final Image image = new Image();
    private Blob blob;

    public BlobField(String label) {
        super();
        setLabel(label);
        add(image);
    }

    @Override
    protected Blob generateModelValue() {
        return blob;
    }

    @Override
    protected void setPresentationValue(@Nullable Blob blob) {
        this.blob = blob;

        if(blob==null) {
            image.setSrc(""); // not sure whether this is correct
            image.setAlt("empty");
            return;
        }

        val streamResource = new StreamResource("isr",
                (InputStreamFactory) () -> new ByteArrayInputStream(blob.getBytes()));
        image.setSrc(streamResource);
        image.setAlt(blob.getName());
    }

}
