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

package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import java.util.Iterator;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;

/**
 * Part of the {@link AjaxFallbackDefaultDataTable} API.
 * 
 * <p>
 * TODO: not yet fully supported.
 */
public class CollectionContentsSortableDataProvider extends SortableDataProvider<ObjectAdapter,String> {

    private static final long serialVersionUID = 1L;

    private final EntityCollectionModel model;

    public CollectionContentsSortableDataProvider(final EntityCollectionModel model) {
        this.model = model;
    }

    @Override
    public Iterator<ObjectAdapter> iterator(final long first, final long count) {
        return model.getObject().subList((int)first, (int)(first + count)).iterator();
    }

    @Override
    public IModel<ObjectAdapter> model(final ObjectAdapter adapter) {
        return new EntityModel(adapter);
    }

    @Override
    public long size() {
        return model.getObject().size();
    }

    @Override
    public void detach() {
        super.detach();
        model.detach();
    }

}