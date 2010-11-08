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


package org.apache.isis.extensions.html.action.view;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.extensions.html.action.Action;
import org.apache.isis.extensions.html.action.ActionException;
import org.apache.isis.extensions.html.action.view.util.TableUtil;
import org.apache.isis.extensions.html.component.Page;
import org.apache.isis.extensions.html.component.Table;
import org.apache.isis.extensions.html.component.ViewPane;
import org.apache.isis.extensions.html.context.Context;
import org.apache.isis.extensions.html.request.Request;



public class CollectionView implements Action {

    public final void execute(final Request request, final Context context, final Page page) {
        final String idString = request.getObjectId();
        final ObjectAdapter collection = context.getMappedCollection(idString);
        if (collection == null) {
            throw new ActionException("No such collection: " + idString);
        }
        final String titleString = collection.titleString();

        page.setTitle(titleString);

        final TypeOfFacet facet = collection.getSpecification().getFacet(TypeOfFacet.class);
        final ObjectSpecification elementSpecification = facet.valueSpec();

        final ViewPane content = page.getViewPane();
        content.setWarningsAndMessages(context.getMessages(), context.getWarnings());
        content.setTitle(titleString, null);
        String iconName = collection.getIconName();
        if (iconName == null) {
            iconName = elementSpecification.getShortName();
        }
        content.setIconName(iconName);

        if (elementSpecification.getAssociations(ObjectAssociationFilters.STATICALLY_VISIBLE_ASSOCIATIONS).length != 0) {
            final Table table = TableUtil.createTable(context, false, collection, titleString, elementSpecification);
            content.add(table);
        } else {
            // TODO this should create a list component instead of a table
            final Table table = TableUtil.createTable(context, false, collection, titleString, elementSpecification);
            content.add(table);
        }

        context.addCollectionCrumb(idString);

        context.addCollectionToHistory(idString);
        context.clearMessagesAndWarnings();
    }

    public String name() {
        return Request.COLLECTION_COMMAND;
    }

}

