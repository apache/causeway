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

package org.apache.isis.viewer.dnd.calendar;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.collection.AbstractCollectionContent;

public class CalendarCellContent extends AbstractCollectionContent {
    private final String title;
    private final List<Object> collection = new ArrayList<Object>();

    public CalendarCellContent(final String title) {
        this.title = title;
    }

    @Override
    public ObjectAdapter getCollection() {
        return IsisContext.getPersistenceSession().getAdapterManager().adapterFor(collection);
    }

    @Override
    public Consent canDrop(final Content sourceContent) {
        return Veto.DEFAULT;
    }

    @Override
    public ObjectAdapter drop(final Content sourceContent) {
        throw new UnexpectedCallException();
    }

    @Override
    public String getHelp() {
        return "No help available";
    }

    @Override
    public String getIconName() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public ObjectAdapter getAdapter() {
        return getCollection();
    }

    @Override
    public ObjectSpecification getSpecification() {
        throw new UnexpectedCallException();
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public String title() {
        return title;
    }

    public void addElement(final ObjectAdapter element) {
        collection.add(element.getObject());
    }

}
