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


package org.apache.isis.extensions.dnd.calendar;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.Veto;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.commons.exceptions.UnexpectedCallException;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.collection.AbstractCollectionContent;
import org.apache.isis.runtime.context.IsisContext;


public class CalendarCellContent extends AbstractCollectionContent {
    private final String title;
    private final List<Object> collection = new ArrayList<Object>();

    public CalendarCellContent(final String title) {
        this.title = title;
    }

    public ObjectAdapter getCollection() {
        return IsisContext.getPersistenceSession().getAdapterManager().adapterFor(collection);
    }

    public Consent canDrop(Content sourceContent) {
        return Veto.DEFAULT;
    }

    public ObjectAdapter drop(Content sourceContent) {
        throw new UnexpectedCallException();
    }

    public String getHelp() {
        return "No help available";
    }

    public String getIconName() {
        return null;
    }

    public String getId() {
        return null;
    }

    public ObjectAdapter getAdapter() {
        return getCollection();
    }

    public ObjectSpecification getSpecification() {
        throw new UnexpectedCallException();
    }

    public boolean isTransient() {
        return true;
    }

    public String title() {
        return title;
    }

    public void addElement(ObjectAdapter element) {
        collection.add(element.getObject());
    }

}

