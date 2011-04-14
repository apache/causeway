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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecification.CreationMode;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;


public class DefaultOidObjectMapping implements ObjectMapping {
    private static final Logger LOG = Logger.getLogger(DefaultOidObjectMapping.class);
    private final Map<String, TransientObjectMapping> requestTransients = new HashMap<String, TransientObjectMapping>();
    private final Map<String, TransientObjectMapping> sessionTransients = new HashMap<String, TransientObjectMapping>();
    private Class<? extends Oid> oidType;

    public void append(DebugBuilder debug) {
        append(debug, requestTransients, "request");
        append(debug, sessionTransients, "session");
    }

    protected void append(DebugBuilder debug, Map<String, TransientObjectMapping> transients, String type) {
        Iterator<String> ids = new HashSet(transients.keySet()).iterator();
        if (ids.hasNext()) {
            debug.appendTitle("Transient objects (" + type + ")");
            while (ids.hasNext()) {
                String key = ids.next();
                debug.appendln(key, transients.get(key).debug());
            }
        }
    }

    public void appendMappings(DebugBuilder request) {}

    public void clear() {
        requestTransients.clear();

        List<String> remove = new ArrayList<String>();
        for (String id : sessionTransients.keySet()) {
            if (!sessionTransients.get(id).getOid().isTransient()) {
                remove.add(id);
                sessionTransients.put(id, null);
            }
        }
        for (String id : remove) {
            sessionTransients.remove(id);
        }
    }

    public void endSession() {
        sessionTransients.clear();
    }

    public String mapTransientObject(ObjectAdapter object) {
        try {
            List<ObjectAdapter> savedObject = new ArrayList<ObjectAdapter>();
            JSONObject data = encodeTransientData(object, savedObject);
            return "D" + StringEscapeUtils.escapeHtml(data.toString(4));
        } catch (JSONException e) {
            throw new ScimpiException(e);
        }
    }

    private JSONObject encodeTransientData(ObjectAdapter object, List<ObjectAdapter> savedObject) throws JSONException {
        if (savedObject.contains(object)) {
            return null;
        }
        savedObject.add(object);

        JSONObject data = new JSONObject();
        ObjectSpecification specification = object.getSpecification();
        data.put("_class", specification.getFullIdentifier());

        Oid oid = object.getOid();
        String encodedOid;
        if (oid instanceof AggregatedOid) {
            AggregatedOid aoid = (AggregatedOid) oid;
            Oid parentOid = aoid.getParentOid();
            object = IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(parentOid);
            String aggregatedId = aoid.getId();
            encodedOid = Long.toString(((SerialOid) parentOid).getSerialNo(), 16) + "@" + aggregatedId;
        } else if (oid instanceof SerialOid) {
            encodedOid = Long.toString(((SerialOid) oid).getSerialNo(), 16);
        } else {
            throw new ScimpiException("Unsupportred OID type " + oid);
        }

        data.put("_id", encodedOid);

        for (ObjectAssociation association : specification.getAssociations()) {
            ObjectAdapter fieldValue = association.get(object);
            String fieldName = association.getId();
            if (fieldValue == null) {
                data.put(fieldName, (Object) null);
            } else if (association.getSpecification().isEncodeable()) {
                EncodableFacet encodeableFacet = fieldValue.getSpecification().getFacet(EncodableFacet.class);
                data.put(fieldName, encodeableFacet.toEncodedString(fieldValue));
            } else if (association instanceof OneToManyAssociation) {
                List<JSONObject> collection = new ArrayList<JSONObject>();
                CollectionFacet facet = fieldValue.getSpecification().getFacet(CollectionFacet.class);
                for (ObjectAdapter element : facet.iterable(fieldValue)) {
                    collection.add(encodeTransientData(element, savedObject));
                }
                data.put(fieldName, collection);
            } else {
                if (fieldValue.isTransient() || fieldValue.isAggregated()) {
                    JSONObject saveData = encodeTransientData(fieldValue, savedObject);
                    if (saveData == null) {
                        data.put(fieldName, mapObject(fieldValue, Scope.INTERACTION));
                    } else {
                        data.put(fieldName, saveData);
                    }
                } else {
                    data.put(fieldName, mapObject(fieldValue, Scope.INTERACTION));
                }
            }
        }
        return data;
    }

    public String mapObject(ObjectAdapter inObject, Scope scope) {
        // TODO need to ensure that transient objects are remapped each time so that any changes are added to
        // session data
        // continue work here.....here

        ObjectAdapter object = inObject;

        Oid oid = object.getOid();
        if (oidType == null) {
            oidType = oid.getClass();
        }

        String encodedOid;
        if (oid instanceof AggregatedOid) {
            AggregatedOid aoid = (AggregatedOid) oid;
            Oid parentOid = aoid.getParentOid();
            object = IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(parentOid);
            String aggregatedId = aoid.getId();
            encodedOid = Long.toString(((SerialOid) parentOid).getSerialNo(), 16) + "@" + aggregatedId;
        } else if (oid instanceof SerialOid) {
            encodedOid = Long.toString(((SerialOid) oid).getSerialNo(), 16);
        } else {
            encodedOid = IsisContext.getPersistenceSession().getOidGenerator().getOidStringifier().enString(oid);
        }

        boolean isTransient = object.isTransient();
        String transferableId = (isTransient ? "T" : "P") + object.getSpecification().getFullIdentifier() + "@" + encodedOid;
        LOG.debug("encoded " + oid + " as " + transferableId + " ~ " + encodedOid);

        if (inObject.isTransient()) {

            // TODO cache these in requests so that Mementos are only created once.
            // TODO if Transient/Interaction then return state; other store state in session an return OID
            // string
            TransientObjectMapping mapping = new TransientObjectMapping(inObject);
            if (scope == Scope.REQUEST) {
                requestTransients.put(transferableId, mapping);
            } else if (scope == Scope.INTERACTION || scope == Scope.SESSION) {
                sessionTransients.put(transferableId, mapping);
            } else {
                throw new ScimpiException("Can't hold globally transient object");
            }
        }
        return transferableId;
    }

    public ObjectAdapter mappedTransientObject(String data) {
        String objectData = StringEscapeUtils.unescapeHtml(data);
        LOG.debug("data" + objectData);

        try {
            JSONObject jsonObject = new JSONObject(objectData);
            ObjectAdapter object = restoreTransientObject(jsonObject);
            return object;
        } catch (JSONException e) {
            throw new ScimpiException("Problem reading data: " + data, e);
        }
    }

    private ObjectAdapter restoreTransientObject(JSONObject jsonObject) throws JSONException {
        String cls = jsonObject.getString("_class");
        String id = jsonObject.getString("_id");

        ObjectAdapter object;
        ObjectSpecification specification = IsisContext.getSpecificationLoader().loadSpecification(cls);
        if (specification.isAggregated() && !specification.isCollection()) {
            String[] split = id.split("@");
            SerialOid parentOid = SerialOid.createTransient(Long.parseLong(split[0], 16));
            AggregatedOid oid = new AggregatedOid(parentOid, split[1]);
            object = IsisContext.getPersistenceSession().recreateAdapter(oid, specification);
        } else {
            object = mappedObject("T" + cls + "@" + id);
        }

        for (ObjectAssociation association : specification.getAssociations()) {
            String fieldName = association.getId();

            Object fieldValue = jsonObject.has(fieldName) ? jsonObject.get(fieldName) : null;

            if (association.getSpecification().isEncodeable()) {
                if (fieldValue == null) {
                    ((OneToOneAssociation) association).initAssociation(object, null);
                } else {
                    EncodableFacet encodeableFacet = association.getSpecification().getFacet(EncodableFacet.class);
                    ObjectAdapter fromEncodedString = encodeableFacet.fromEncodedString((String) fieldValue);
                    ((OneToOneAssociation) association).initAssociation(object, fromEncodedString);
                }
            } else if (association instanceof OneToManyAssociation) {
                List<JSONObject> collection = new ArrayList<JSONObject>();
                if (!collection.isEmpty()) {
                    throw new ScimpiException("Unexpected association for transient object " + association);
                }
                /*
                CollectionFacet facet = fieldValue.getSpecification().getFacet(CollectionFacet.class);
                for (ObjectAdapter element : facet.iterable(fieldValue)) {
                   collection.add(saveData(element, savedObject));
                }
                data.put(fieldName, collection);
                */
            } else {
                if (fieldValue == null) {
                    ((OneToOneAssociation) association).initAssociation(object, null);
                } else {
                    if (fieldValue instanceof JSONObject) {
                        ObjectAdapter fieldObject = restoreTransientObject((JSONObject) fieldValue);
                        ((OneToOneAssociation) association).initAssociation(object, fieldObject);
                    } else {
                        ObjectAdapter field = mappedObject((String) fieldValue);
                        ((OneToOneAssociation) association).initAssociation(object, field);
                    }
                }
            }
        }
        return object;
    }

    public ObjectAdapter mappedObject(String id) {
        char type = id.charAt(0);
        if ((type == 'T')) {
            TransientObjectMapping mapping = sessionTransients.get(id);
            if (mapping == null) {
                mapping = requestTransients.get(id);
            }
            if (mapping == null) {
                String[] split = id.split("@");
                ObjectSpecification spec = IsisContext.getSpecificationLoader().loadSpecification(split[0].substring(1));
                Object pojo = spec.createObject(CreationMode.NO_INITIALIZE);
                String oidData = split[1];
                SerialOid oid = SerialOid.createTransient(Long.valueOf(oidData, 16).longValue());
                return IsisContext.getPersistenceSession().recreateAdapter(oid, pojo);
            }
            ObjectAdapter mappedTransientObject = mapping.getObject();
            LOG.debug("retrieved " + mappedTransientObject.getOid() + " for " + id);
            return mappedTransientObject;
        } else {
            String[] split = id.split("@");
            ObjectSpecification spec = IsisContext.getSpecificationLoader().loadSpecification(split[0].substring(1));

            try {
                String oidData = split[1];
                LOG.debug("decoding " + oidData);

                ObjectAdapter loadObject;
                Oid oid;
                // HACK - to remove after fix!
                if (oidType == null) {
                    oidType = IsisContext.getPersistenceSession().getServices().get(0).getOid().getClass();
                }
                if (split.length > 2) {
                    SerialOid parentOid = SerialOid.createPersistent(Long.parseLong(oidData, 16));
                    oid = new AggregatedOid(parentOid, split[2]);
                    IsisContext.getPersistenceSession().loadObject(parentOid, spec);
                    loadObject = IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(oid);
                } else if (oidType.isAssignableFrom(SerialOid.class)) {
                    oid = SerialOid.createPersistent(Long.parseLong(oidData, 16));
                    loadObject = IsisContext.getPersistenceSession().loadObject(oid, spec);
                } else {
                    oid = IsisContext.getPersistenceSession().getOidGenerator().getOidStringifier().deString(oidData);
                    loadObject = IsisContext.getPersistenceSession().loadObject(oid, spec);
                }

                return loadObject;
            } catch (SecurityException e) {
                throw new IsisException(e);
            }
        }
    }

    public void reloadIdentityMap() {
        Iterator<TransientObjectMapping> mappings = sessionTransients.values().iterator();
        while (mappings.hasNext()) {
            TransientObjectMapping mapping = mappings.next();
            mapping.reload();
        }
    }

    public void unmapObject(ObjectAdapter object, Scope scope) {
        sessionTransients.remove(object.getOid());
        requestTransients.remove(object.getOid());
    }

}
