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
package org.apache.isis.valuetypes.asciidoc.ui.wkt.components;

import org.apache.wicket.model.IModel;

import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.val;

public class AsciiDocForXmlComponentWkt extends AsciiDocComponentWkt {

    private static final long serialVersionUID = 1L;

    public AsciiDocForXmlComponentWkt(final String id, final IModel<?> model) {
        super(id, model);
        setEnabled(false);
    }

    /**
     * for convenience of subtypes.
     * @param xml
     */
    protected final String asHtml(final String xml) {
        val adoc = "[source,xml]\n----\n" + xml + "\n----";
        return AsciiDoc.valueOf(adoc).asHtml();
    }

}
