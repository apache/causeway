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
package org.apache.causeway.viewer.wicket.ui.components.about;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import org.apache.causeway.viewer.wicket.ui.util.Wkt;

public final class JarManifestListView extends ListView<JarManifestAttributes> {

    private static final long serialVersionUID = 1L;
    private final String idLine;

    public JarManifestListView(final String id, final String idLine, final List<JarManifestAttributes> list) {
        super(id, list);
        this.idLine = idLine;
    }

    @Override
    protected void populateItem(final ListItem<JarManifestAttributes> item) {
        final JarManifestAttributes detail = item.getModelObject();
        item.add(new AttributeAppender("class", detail.getType().name().toLowerCase()));
        Wkt.labelAdd(item, idLine, detail.getLine());
    }
}
