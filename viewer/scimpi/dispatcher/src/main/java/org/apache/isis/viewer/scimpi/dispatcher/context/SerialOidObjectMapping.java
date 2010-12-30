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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugView;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

/**
 * @Deprecated
 * Replaced by {@link DefaultOidObjectMapping}
 */
public class SerialOidObjectMapping implements ObjectMapping {
    private static final int RADIX = Character.MAX_RADIX;
    private final Map<String, TransientObjectMapping> transients = new HashMap<String, TransientObjectMapping>();

    public void append(DebugView view) {
        Iterator<String> ids = new HashSet(transients.keySet()).iterator();
        if (ids.hasNext()) {
            view.divider("Transient objects");
            while (ids.hasNext()) {
                String key = ids.next();
                view.appendRow(key, transients.get(key).debug());
            }
        }
    }

    public void appendMappings(Request request) {}

    public void clear(Scope scope) {
        Iterator<TransientObjectMapping> mapping = transients.values().iterator();
        while (mapping.hasNext()) {
            mapping.next().update();
        }
    }

    public void endSession() {}

    public String mapObject(ObjectAdapter object, Scope scope) {

        // TODO need to ensure that transient objects are remapped each time so that any changes are added to
        // session data
        // continue work here.....here

        SerialOid oid = (SerialOid) ((ObjectAdapter) object).getOid();
        boolean isTransient = oid.isTransient();
        String id = (isTransient ? "T" : "P") + object.getSpecification().getFullIdentifier() + "@"
                + Long.toString(oid.getSerialNo(), RADIX);
        if (isTransient) {
            transients.put(id, new TransientObjectMapping((ObjectAdapter) object));
        }
        return id;
    }

    public ObjectAdapter mappedObject(String id) {
        boolean isTransient = id.charAt(0) == 'T';
        if (isTransient) {
            TransientObjectMapping mapping = transients.get(id);
            if (mapping == null) {
                return null;
            }
            return mapping.getObject();
        } else {
            int pos = id.indexOf('@');
            if (pos == -1) {
                return null;
            }
            ObjectSpecification spec = IsisContext.getSpecificationLoader().loadSpecification(id.substring(1, pos));
            long serialNumber = Long.parseLong(id.substring(pos + 1), RADIX);
            SerialOid oid = isTransient ? SerialOid.createTransient(serialNumber) : SerialOid.createPersistent(serialNumber);
            return IsisContext.getPersistenceSession().loadObject(oid, spec);
        }
    }

    public void reloadIdentityMap() {
        Iterator<TransientObjectMapping> mappings = transients.values().iterator();
        while (mappings.hasNext()) {
            TransientObjectMapping mapping = mappings.next();
            mapping.reload();
        }
    }

    public void unmapObject(ObjectAdapter object, Scope scope) {
        transients.remove(object.getOid());
    }

}
