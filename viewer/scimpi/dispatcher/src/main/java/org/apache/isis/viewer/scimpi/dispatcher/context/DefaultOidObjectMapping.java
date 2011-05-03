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
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
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

    @Override
    public void append(final DebugBuilder debug) {
        append(debug, requestTransients, "request");
        append(debug, sessionTransients, "session");
    }

    protected void append(final DebugBuilder debug, final Map<String, TransientObjectMapping> transients,
        final String type) {
        final Iterator<String> ids = new HashSet(transients.keySet()).iterator();
        if (ids.hasNext()) {
            debug.appendTitle("Transient objects (" + type + ")");
            while (ids.hasNext()) {
                final String key = ids.next();
                debug.appendln(key, transients.get(key).debug());
            }
        }
    }

    @Override
    public void appendMappings(final DebugBuilder request) {
    }

    @Override
    public void clear() {
        requestTransients.clear();

        final List<String> remove = new ArrayList<String>();
        for (final String id : sessionTransients.keySet()) {
            if (!sessionTransients.get(id).getOid().isTransient()) {
                remove.add(id);
                sessionTransients.put(id, null);
            }
        }
        for (final String id : remove) {
            sessionTransients.remove(id);
        }
    }

    @Override
    public void endSession() {
        sessionTransients.clear();
    }

    @Override
    public String mapTransientObject(final ObjectAdapter object) {
        try {
            final List<ObjectAdapter> savedObject = new ArrayList<ObjectAdapter>();
            final JSONObject data = encodeTransientData(object, savedObject);
            return "D" + data.toString(4); // StringEscapeUtils.escapeHtml(data.toString(4));
        } catch (final JSONException e) {
            throw new ScimpiException(e);
        }
    }

    private JSONObject encodeTransientData(final ObjectAdapter object, final List<ObjectAdapter> savedObject)
        throws JSONException {
        if (savedObject.contains(object)) {
            return null;
        }
        savedObject.add(object);

        final JSONObject data = new JSONObject();
        final ObjectSpecification specification = object.getSpecification();
        data.put("_class", specification.getFullIdentifier());

        final Oid oid = object.getOid();
        String encodedOid;
        if (oid instanceof AggregatedOid) {
            final AggregatedOid aoid = (AggregatedOid) oid;
            final Oid parentOid = aoid.getParentOid();
            final String aggregatedId = aoid.getId();
            encodedOid = Long.toString(((SerialOid) parentOid).getSerialNo(), 16) + "@" + aggregatedId;
        } else if (oid instanceof SerialOid) {
            encodedOid = Long.toString(((SerialOid) oid).getSerialNo(), 16);
        } else {
            throw new ScimpiException("Unsupportred OID type " + oid);
        }

        data.put("_id", encodedOid);

        for (final ObjectAssociation association : specification.getAssociations()) {
            final ObjectAdapter fieldValue = association.get(object);
            final String fieldName = association.getId();
            if (fieldValue == null) {
                data.put(fieldName, (Object) null);
            } else if (association.getSpecification().isEncodeable()) {
                final EncodableFacet encodeableFacet = fieldValue.getSpecification().getFacet(EncodableFacet.class);
                data.put(fieldName, encodeableFacet.toEncodedString(fieldValue));
            } else if (association instanceof OneToManyAssociation) {
                final List<JSONObject> collection = new ArrayList<JSONObject>();
                final CollectionFacet facet = fieldValue.getSpecification().getFacet(CollectionFacet.class);
                for (final ObjectAdapter element : facet.iterable(fieldValue)) {
                    collection.add(encodeTransientData(element, savedObject));
                }
                data.put(fieldName, collection);
            } else {
                if (fieldValue.isTransient() || fieldValue.isAggregated()) {
                    final JSONObject saveData = encodeTransientData(fieldValue, savedObject);
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

    @Override
    public String mapObject(final ObjectAdapter inObject, final Scope scope) {
        // TODO need to ensure that transient objects are remapped each time so that any changes are added to
        // session data
        // continue work here.....here

        ObjectAdapter object = inObject;

        final Oid oid = object.getOid();
        if (oidType == null) {
            oidType = oid.getClass();
        }

        String encodedOid;
        if (oid instanceof AggregatedOid) {
            final AggregatedOid aoid = (AggregatedOid) oid;
            final Oid parentOid = aoid.getParentOid();
            object = IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(parentOid);
            final String aggregatedId = aoid.getId();
            encodedOid = Long.toString(((SerialOid) parentOid).getSerialNo(), 16) + "@" + aggregatedId;
        } else if (oid instanceof SerialOid) {
            encodedOid = Long.toString(((SerialOid) oid).getSerialNo(), 16);
        } else {
            encodedOid = IsisContext.getPersistenceSession().getOidGenerator().getOidStringifier().enString(oid);
        }

        final boolean isTransient = object.isTransient();
        final String transferableId =
            (isTransient ? "T" : "P") + object.getSpecification().getFullIdentifier() + "@" + encodedOid;
        LOG.debug("encoded " + oid + " as " + transferableId + " ~ " + encodedOid);

        if (inObject.isTransient()) {

            // TODO cache these in requests so that Mementos are only created once.
            // TODO if Transient/Interaction then return state; other store state in session an return OID
            // string
            final TransientObjectMapping mapping = new TransientObjectMapping(inObject);
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

    @Override
    public ObjectAdapter mappedTransientObject(final String data) {
        final String objectData = data; // StringEscapeUtils.unescapeHtml(data);
        LOG.debug("data" + objectData);

        try {
            final JSONObject jsonObject = new JSONObject(objectData);
            final ObjectAdapter object = restoreTransientObject(jsonObject);
            return object;
        } catch (final JSONException e) {
            throw new ScimpiException("Problem reading data: " + data, e);
        }
    }

    private ObjectAdapter restoreTransientObject(final JSONObject jsonObject) throws JSONException {
        final String cls = jsonObject.getString("_class");
        final String id = jsonObject.getString("_id");

        ObjectAdapter object;
        final ObjectSpecification specification = IsisContext.getSpecificationLoader().loadSpecification(cls);
        if (specification.isAggregated() && !specification.isCollection()) {
            final String[] split = id.split("@");
            final SerialOid parentOid = SerialOid.createTransient(Long.parseLong(split[0], 16));
            final AggregatedOid oid = new AggregatedOid(parentOid, split[1]);
            object = IsisContext.getPersistenceSession().recreateAdapter(oid, specification);
        } else {
            object = mappedObject("T" + cls + "@" + id);
        }

        for (final ObjectAssociation association : specification.getAssociations()) {
            final String fieldName = association.getId();

            final Object fieldValue = jsonObject.has(fieldName) ? jsonObject.get(fieldName) : null;

            if (association.getSpecification().isEncodeable()) {
                if (fieldValue == null) {
                    ((OneToOneAssociation) association).initAssociation(object, null);
                } else {
                    final EncodableFacet encodeableFacet =
                        association.getSpecification().getFacet(EncodableFacet.class);
                    final ObjectAdapter fromEncodedString = encodeableFacet.fromEncodedString((String) fieldValue);
                    ((OneToOneAssociation) association).initAssociation(object, fromEncodedString);
                }
            } else if (association instanceof OneToManyAssociation) {
                final List<JSONObject> collection = new ArrayList<JSONObject>();
                if (!collection.isEmpty()) {
                    throw new ScimpiException("Unexpected association for transient object " + association);
                }
                /*
                 * CollectionFacet facet = fieldValue.getSpecification().getFacet(CollectionFacet.class); for
                 * (ObjectAdapter element : facet.iterable(fieldValue)) { collection.add(saveData(element,
                 * savedObject)); } data.put(fieldName, collection);
                 */
            } else {
                if (fieldValue == null) {
                    ((OneToOneAssociation) association).initAssociation(object, null);
                } else {
                    if (fieldValue instanceof JSONObject) {
                        final ObjectAdapter fieldObject = restoreTransientObject((JSONObject) fieldValue);
                        ((OneToOneAssociation) association).initAssociation(object, fieldObject);
                    } else {
                        final ObjectAdapter field = mappedObject((String) fieldValue);
                        ((OneToOneAssociation) association).initAssociation(object, field);
                    }
                }
            }
        }
        return object;
    }

    @Override
    public ObjectAdapter mappedObject(final String id) {
        final char type = id.charAt(0);
        if ((type == 'T')) {
            TransientObjectMapping mapping = sessionTransients.get(id);
            if (mapping == null) {
                mapping = requestTransients.get(id);
            }
            if (mapping == null) {
                final String[] split = id.split("@");
                final ObjectSpecification spec =
                    IsisContext.getSpecificationLoader().loadSpecification(split[0].substring(1));
                final Object pojo = spec.createObject(CreationMode.NO_INITIALIZE);
                final String oidData = split[1];
                final SerialOid oid = SerialOid.createTransient(Long.valueOf(oidData, 16).longValue());
                return IsisContext.getPersistenceSession().recreateAdapter(oid, pojo);
            }
            final ObjectAdapter mappedTransientObject = mapping.getObject();
            LOG.debug("retrieved " + mappedTransientObject.getOid() + " for " + id);
            return mappedTransientObject;
        } else {
            final String[] split = id.split("@");
            final ObjectSpecification spec =
                IsisContext.getSpecificationLoader().loadSpecification(split[0].substring(1));

            try {
                final String oidData = split[1];
                LOG.debug("decoding " + oidData);

                ObjectAdapter loadObject;
                Oid oid;
                // HACK - to remove after fix!
                if (oidType == null) {
                    oidType = IsisContext.getPersistenceSession().getServices().get(0).getOid().getClass();
                }
                if (split.length > 2) {
                    final SerialOid parentOid = SerialOid.createPersistent(Long.parseLong(oidData, 16));
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
            } catch (final SecurityException e) {
                throw new IsisException(e);
            }
        }
    }

    @Override
    public void reloadIdentityMap() {
        final Iterator<TransientObjectMapping> mappings = sessionTransients.values().iterator();
        while (mappings.hasNext()) {
            final TransientObjectMapping mapping = mappings.next();
            mapping.reload();
        }
    }

    @Override
    public void unmapObject(final ObjectAdapter object, final Scope scope) {
        sessionTransients.remove(object.getOid());
        requestTransients.remove(object.getOid());
    }

}
