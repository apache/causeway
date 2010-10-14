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


package org.apache.isis.webapp.context;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.encoding.DataOutputExtended;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.webapp.context.RequestContext.Scope;
import org.apache.isis.webapp.debug.DebugView;
import org.apache.isis.webapp.processor.Request;


public class HibernateOidObjectMapping implements ObjectMapping {
    
    static class HibernateOid implements Oid {

        public void clearPrevious() {}

        public void copyFrom(Oid oid) {}

        public Oid getPrevious() {
            return null;
        }

        public boolean hasPrevious() {
            return false;
        }

        public boolean isTransient() {
            return false;
        }

        public void makePersistent() {}

        public void encode(DataOutputExtended outputStream) throws IOException {}

        public String getClassName() {
            return null;
        }

        public String getPrimaryKey() {
            return null;
        }

        public static HibernateOid createTransient(String className, Serializable key) {
            return null;
        }

        public static HibernateOid createPersistent(String className, Serializable key, Serializable key2) {
            return null;
        }
        
    }
    
    
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

        HibernateOid oid = (HibernateOid) ((ObjectAdapter) object).getOid();
        boolean isTransient = oid.isTransient();
        String id = (isTransient ? "T" : "P") + oid.getClassName() + "@" + (oid.getPrimaryKey() instanceof String ? "$" : "*")
                + oid.getPrimaryKey();
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
            String className = id.substring(1, pos);
            String type = id.substring(pos + 1, pos + 2);
            ObjectSpecification spec = IsisContext.getSpecificationLoader().loadSpecification(className);
            Serializable key;
            if (type.equals("$")) {
                key = id.substring(pos + 2);
            } else {
                key = Long.valueOf(id.substring(pos + 2));
            }
            HibernateOid oid;
            if (isTransient) {
                oid = HibernateOid.createTransient(className, key);
            } else {
                oid =HibernateOid.createPersistent(className, key, key);

            }
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
