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


package org.apache.isis.extensions.dnd.view.content;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.extensions.dnd.view.Content;


public class RootObject extends AbstractObjectContent {
    private final ObjectAdapter adapter;

    public RootObject(final ObjectAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public Consent canClear() {
        return Veto.DEFAULT;
    }

    @Override
    public Consent canDrop(final Content sourceContent) {
        return super.canDrop(sourceContent);
    }

    @Override
    public Consent canSet(final ObjectAdapter dragSource) {
        return Veto.DEFAULT;
    }

    @Override
    public void clear() {
        throw new IsisException("Invalid call");
    }

    public void debugDetails(final DebugString debug) {
        debug.appendln("object", adapter);
    }

    public ObjectAdapter getAdapter() {
        return adapter;
    }

    public String getDescription() {
        return getSpecification().getSingularName() + ": " + getObject().titleString() + " "
                + getSpecification().getDescription();
    }

    public String getHelp() {
        return "";
    }

    public String getId() {
        return "";
    }

    @Override
    public ObjectAdapter getObject() {
        return adapter;
    }

    public ObjectAdapter[] getOptions() {
        return null;
    }

    public ObjectSpecification getSpecification() {
        return adapter.getSpecification();
    }

    @Override
    public boolean isObject() {
        return true;
    }

    public boolean isOptionEnabled() {
        return false;
    }

    public boolean isTransient() {
        return adapter != null && adapter.isTransient();
    }

    @Override
    public void setObject(final ObjectAdapter object) {
        throw new IsisException("Invalid call");
    }

    public String title() {
        return adapter.titleString();
    }

    @Override
    public String toString() {
        return "Root Object [adapter=" + adapter + "]";
    }

    @Override
    public String windowTitle() {
        return (isTransient() ? "UNSAVED " : "") + getSpecification().getSingularName();
    }
}
