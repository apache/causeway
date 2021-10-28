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
package org.apache.isis.viewer.wicket.ui.errors;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import org.apache.isis.viewer.wicket.ui.util.Wkt;

public final class StackTraceListView
extends ListView<StackTraceDetail> {

    private static final long serialVersionUID = 1L;
    private final String idLine;

    public StackTraceListView(final String id, final String idLine, final List<org.apache.isis.viewer.wicket.ui.errors.StackTraceDetail> list) {
        super(id, list);
        this.idLine = idLine;
    }

    @Override
    protected void populateItem(final ListItem<StackTraceDetail> item) {
        final StackTraceDetail detail = item.getModelObject();
        item.add(new AttributeAppender("class", detail.getType().name().toLowerCase()));
        Wkt.labelAdd(item, idLine, detail.getLine());
    }
}
