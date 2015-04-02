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

package org.apache.isis.viewer.scimpi.dispatcher.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import com.google.common.collect.Maps;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;

public class IndirectObjectMapping implements ObjectMapping {
    private final Map<Scope, Map<String, Mapping>> scopedMappings = Maps.newLinkedHashMap();
    private int nextId = 0;

    public IndirectObjectMapping() {
        scopedMappings.put(Scope.GLOBAL, Maps.<String,Mapping>newHashMap());
        scopedMappings.put(Scope.SESSION, Maps.<String,Mapping>newHashMap());
        scopedMappings.put(Scope.INTERACTION, Maps.<String,Mapping>newHashMap());
        scopedMappings.put(Scope.REQUEST, Maps.<String,Mapping>newHashMap());
    }

    private String nextId() {
        nextId++;
        return String.valueOf(nextId);
    }

    @Override
    public void endSession() {
        scopedMappings.get(Scope.SESSION).clear();
        nextId = 0;
    }

    @Override
    public void reloadIdentityMap() {
        reloadIdentityMap(Scope.GLOBAL);
        reloadIdentityMap(Scope.SESSION);
        reloadIdentityMap(Scope.INTERACTION);

        final Map<String, Mapping> map = scopedMappings.get(Scope.INTERACTION);
        scopedMappings.put(Scope.REQUEST, map);
        scopedMappings.put(Scope.INTERACTION, new HashMap<String, Mapping>());
    }

    private void reloadIdentityMap(final Scope scope) {
        final Map<String, Mapping> map = scopedMappings.get(scope);
        final Iterator<String> ids = map.keySet().iterator();
        while (ids.hasNext()) {
            final String key = ids.next();
            final Mapping mapping = map.get(key);
            mapping.reload();
        }
    }

    @Override
    public void clear() {
        scopedMappings.get(Scope.REQUEST).clear();
    }

    @Override
    public void unmapObject(final ObjectAdapter object, final Scope scope) {
        final String id = mapObject(object, scope);
        scopedMappings.get(scope).remove(id);
    }

    @Override
    public void appendMappings(final DebugBuilder debug) {
        appendMappings(debug, scopedMappings.get(Scope.INTERACTION));
    }

    private void appendMappings(final DebugBuilder debug, final Map<String, Mapping> map) {
        final Iterator<String> names = map.keySet().iterator();
        while (names.hasNext()) {
            final String id = names.next();
            final ObjectAdapter object = mappedObject(id);
            debug.appendln(id, object);
        }
    }

    private void appendMappings(final DebugBuilder debug, final Scope scope) {
        debug.appendTitle("Objects for " + scope);
        final Map<String, Mapping> map = scopedMappings.get(scope);
        final Iterator<String> ids = new TreeSet<String>(map.keySet()).iterator();
        if (!ids.hasNext()) {
            debug.appendln("None", "");
        }
        while (ids.hasNext()) {
            final String key = ids.next();
            debug.appendln(key, map.get(key).debug());
        }
    }

    private Mapping createMapping(final ObjectAdapter adapter) {
        if (adapter.isTransient()) {
            return new TransientRootAdapterMapping(adapter);
        } else {
            return new PersistentRootAdapterMapping(adapter);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.isis.webapp.context.ObjectMapping#mappedObject(java.lang.String
     * )
     */
    @Override
    public ObjectAdapter mappedObject(final String id) {
        final Iterator<Map<String, Mapping>> iterator = scopedMappings.values().iterator();
        while (iterator.hasNext()) {
            final Map<String, Mapping> map = iterator.next();
            final Mapping mapping = map.get(id);
            if (mapping != null) {
                return mapping.getObject();
            }
        }
        return null;
    }

    @Override
    public String mapObject(final ObjectAdapter obj, final Scope scope) {
        ObjectAdapter object;
        object = obj;
        final Mapping mapping = createMapping(object);

        boolean changeScope = false;
        for (final Scope s : scopedMappings.keySet()) {
            final Map<String, Mapping> map = scopedMappings.get(s);
            if (map.containsValue(mapping)) {
                final String id = findMapping(map, mapping);
                if (changeScope) {
                    map.remove(id);
                    scopedMappings.get(scope).put(id, mapping);
                }
                return id;
            }

            if (s == scope) {
                changeScope = true;
            }
        }

        final Map<String, Mapping> map = scopedMappings.get(scope);
        final String id = obj.getSpecification().getShortIdentifier() + "@" + nextId();
        map.put(id, mapping);
        return id;
    }

    private String findMapping(final Map<String, Mapping> map, final Mapping mapping) {
        final Iterator<String> ids = map.keySet().iterator();
        while (ids.hasNext()) {
            final String key = ids.next();
            if (map.get(key).equals(mapping)) {
                return key;
            }
        }

        return null;
    }

    @Override
    public void append(final DebugBuilder debug) {
        debug.appendln("Next ID", nextId);

        appendMappings(debug, Scope.GLOBAL);
        appendMappings(debug, Scope.SESSION);
        appendMappings(debug, Scope.INTERACTION);
        appendMappings(debug, Scope.REQUEST);
    }

    @Override
    public ObjectAdapter mappedTransientObject(final String substring) {
        throw new NotYetImplementedException();
    }

    @Override
    public String mapTransientObject(final ObjectAdapter object) {
        throw new NotYetImplementedException();
    }

}
