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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;


public class IndirectObjectMapping implements ObjectMapping {
    private final Map<Scope, Map<String, Mapping>> scopedMappings = new LinkedHashMap<Scope, Map<String, Mapping>>();
    private int nextId = 0;

    public IndirectObjectMapping() {
        scopedMappings.put(Scope.GLOBAL, new HashMap<String, Mapping>());
        scopedMappings.put(Scope.SESSION, new HashMap<String, Mapping>());
        scopedMappings.put(Scope.INTERACTION, new HashMap<String, Mapping>());
        scopedMappings.put(Scope.REQUEST, new HashMap<String, Mapping>());
    }

    private String nextId() {
        nextId++;
        return String.valueOf(nextId);
    }

    public void endSession() {
        scopedMappings.get(Scope.SESSION).clear();
        nextId = 0;
    }

    public void reloadIdentityMap() {
        reloadIdentityMap(Scope.GLOBAL);
        reloadIdentityMap(Scope.SESSION);
        reloadIdentityMap(Scope.INTERACTION);
        
        Map<String, Mapping> map = scopedMappings.get(Scope.INTERACTION);
        scopedMappings.put(Scope.REQUEST, map);
        scopedMappings.put(Scope.INTERACTION, new HashMap<String, Mapping>());
    }

    private void reloadIdentityMap(Scope scope) {
        Map<String, Mapping> map = scopedMappings.get(scope);
        Iterator<String> ids = map.keySet().iterator();
        while (ids.hasNext()) {
            String key = ids.next();
            Mapping mapping = map.get(key);
            mapping.reload();
        }
    }

    public void clear() {
        scopedMappings.get(Scope.REQUEST).clear();
    }

    public void unmapObject(ObjectAdapter object, Scope scope) {
        String id = mapObject(object, scope);
        scopedMappings.get(scope).remove(id);
    }

    public void appendMappings(DebugBuilder debug) {
        appendMappings(debug, scopedMappings.get(Scope.INTERACTION));
    }

    private void appendMappings(DebugBuilder debug, Map<String, Mapping> map) {
        Iterator<String> names = map.keySet().iterator();
        while (names.hasNext()) {
            String id = names.next();
            ObjectAdapter object = mappedObject(id);
            debug.appendln(id, object);
        }
    }

    private void appendMappings(DebugBuilder debug, Scope scope) {
        debug.appendTitle("Objects for " + scope);
        Map<String, Mapping> map = scopedMappings.get(scope);
        Iterator<String> ids = new TreeSet(map.keySet()).iterator();
        if (!ids.hasNext()) {
            debug.appendln("None", "");
        }
        while (ids.hasNext()) {
            String key = ids.next();
            debug.appendln(key, map.get(key).debug());
        }
    }

    private Mapping createMapping(ObjectAdapter adapter) {
        if (adapter.getResolveState().isTransient()) {
            return new TransientObjectMapping(adapter);
        } else {
            return new PersistentObjectMapping(adapter);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.isis.webapp.context.ObjectMapping#mappedObject(java.lang.String)
     */
    public ObjectAdapter mappedObject(String id) {
        Iterator<Map<String, Mapping>> iterator = scopedMappings.values().iterator();
        while (iterator.hasNext()) {
            Map<String, Mapping> map = iterator.next();
            Mapping mapping = map.get(id);
            if (mapping != null) {
                return mapping.getObject();
            }
        }
        return null;
    }

    public String mapObject(ObjectAdapter obj, Scope scope) {
        ObjectAdapter object;
        object = (ObjectAdapter) obj;
        Mapping mapping = createMapping(object);
        
        boolean changeScope = false;
        for (Scope s : scopedMappings.keySet()) {
            Map<String, Mapping> map = scopedMappings.get(s);
            if (map.containsValue(mapping)) {
                String id = findMapping(map, mapping);
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
        
        Map<String, Mapping> map = scopedMappings.get(scope);
        String id = obj.getSpecification().getShortIdentifier() + "@" + nextId();
        map.put(id, mapping);
        return id;
    }

    private String findMapping(Map<String, Mapping> map, Mapping mapping) {
        Iterator<String> ids = map.keySet().iterator();
        while (ids.hasNext()) {
            String key = ids.next();
            if (map.get(key).equals(mapping)) {
                return key;
            }
        }

        return null;
    }

    public void append(DebugBuilder debug) {
        debug.appendln("Next ID", nextId);

        appendMappings(debug, Scope.GLOBAL);
        appendMappings(debug, Scope.SESSION);
        appendMappings(debug, Scope.INTERACTION);
        appendMappings(debug, Scope.REQUEST);
    }

    public ObjectAdapter decodeObject(String substring) {
        throw new NotYetImplementedException();
    }

    public String encodedObject(ObjectAdapter object) {
        throw new NotYetImplementedException();
    }

}
