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


package org.apache.isis.core.metamodel.specloader.internal.peer;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.specloader.ReflectiveActionException;


public abstract class ObjectActionPeerDecorator implements ObjectActionPeer {
    private final ObjectActionPeer decorated;

    public ObjectActionPeerDecorator(final ObjectActionPeer decorated) {
        this.decorated = decorated;
    }

    public void debugData(final DebugString debugString) {
        decorated.debugData(debugString);
    }

    public ObjectAdapter execute(final ObjectAdapter object, final ObjectAdapter[] parameters) throws ReflectiveActionException {
        return decorated.execute(object, parameters);
    }

    public <T extends Facet> T getFacet(final Class<T> cls) {
        return decorated.getFacet(cls);
    }

    public Class<? extends Facet>[] getFacetTypes() {
        return decorated.getFacetTypes();
    }

    public Facet[] getFacets(final Filter<Facet> filter) {
        return decorated.getFacets(filter);
    }

    public Identifier getIdentifier() {
        return decorated.getIdentifier();
    }

    public ObjectActionParamPeer[] getParameters() {
        return decorated.getParameters();
    }

}
