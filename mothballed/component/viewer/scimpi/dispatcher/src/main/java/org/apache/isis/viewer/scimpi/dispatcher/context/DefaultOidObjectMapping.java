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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;

public class DefaultOidObjectMapping implements ObjectMapping {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultOidObjectMapping.class);

    private final Map<String, TransientRootAdapterMapping> requestTransients = Maps.newHashMap();
    private final Map<String, TransientRootAdapterMapping> sessionTransients = Maps.newHashMap();

    //private Class<? extends Oid> oidType;


    ///////////////////////////////////////
    // clear, endSession
    ///////////////////////////////////////

    @Override
    public void clear() {
        requestTransients.clear();

        final List<String> remove = Lists.newArrayList();
        for (final String id : sessionTransients.keySet()) {
            final Oid oid = sessionTransients.get(id).getOid();
            if (!oid.isTransient()) {
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


    ///////////////////////////////////////
    // mapTransientObject
    ///////////////////////////////////////

    @Override
    public String mapTransientObject(final ObjectAdapter adapter) {
        try {
            final List<ObjectAdapter> savedObject = Lists.newArrayList();
            final JSONObject data = encodeTransientData(adapter, savedObject);
            return RequestContext.TRANSIENT_OBJECT_OID_MARKER + data.toString(4);
        } catch (final JSONException e) {
            throw new ScimpiException(e);
        }
    }

    private JSONObject encodeTransientData(final ObjectAdapter adapter, final List<ObjectAdapter> adaptersToSave) throws JSONException {
        if (adaptersToSave.contains(adapter)) {
            return null;
        }
        adaptersToSave.add(adapter);

        final JSONObject data = createJsonForAdapter(adapter);

        final ObjectSpecification specification = adapter.getSpecification();
        for (final ObjectAssociation association : specification.getAssociations(Contributed.EXCLUDED)) {
            final ObjectAdapter fieldValue = association.get(adapter);
            final String fieldName = association.getId();

            if (fieldValue == null) {
                data.put(fieldName, (Object) null);
            } else if (association.getSpecification().isEncodeable()) {
                final EncodableFacet encodeableFacet = fieldValue.getSpecification().getFacet(EncodableFacet.class);
                data.put(fieldName, encodeableFacet.toEncodedString(fieldValue));

            } else if (association instanceof OneToManyAssociation) {
                final List<JSONObject> collection = Lists.newArrayList();
                final CollectionFacet facet = fieldValue.getSpecification().getFacet(CollectionFacet.class);
                for (final ObjectAdapter element : facet.iterable(fieldValue)) {
                    collection.add(encodeTransientData(element, adaptersToSave));
                }
                data.put(fieldName, collection);
            } else {
                if (fieldValue.isTransient() || fieldValue.isParented()) {
                    final JSONObject saveData = encodeTransientData(fieldValue, adaptersToSave);
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

    private JSONObject createJsonForAdapter(final ObjectAdapter adapter) throws JSONException {
        final JSONObject data = new JSONObject();

        final Oid oid = adapter.getOid();
        data.put("_oid", oid.enString(getOidMarshaller()));

        if(oid instanceof RootOid) {
            return data;
        }

        if (!(oid instanceof AggregatedOid)) {
            throw new ScimpiException("Unsupported OID type " + oid);
        }
        return data;
    }




    ////////////////////////////////////////////////////
    // mapObject  (either persistent or transient)
    ////////////////////////////////////////////////////

    @Override
    public String mapObject(final ObjectAdapter adapter, final Scope scope) {

        // TODO need to ensure that transient objects are remapped each time so
        // that any changes are added to
        // session data
        // continue work here.....here

        final Oid oid = adapter.getOid();
//        if (oidType == null) {
//            oidType = oid.getClass();
//        }

        String encodedOid = oid.enString(getOidMarshaller());

        //final boolean isTransient = adapter.isTransient();
        //final String transferableId = (isTransient ? "T" : "P") + adapter.getSpecification().getFullIdentifier() + "@" + encodedOid;
        final String transferableId = encodedOid;
        // LOG.debug("encoded " + oid + " as " + transferableId + " ~ " + encodedOid);

        if (adapter.isTransient()) {
            // old TODO cache these in requests so that Mementos are only created once.
            // old TODO if Transient/Interaction then return state; other store state in session an return OID string
            final TransientRootAdapterMapping mapping = new TransientRootAdapterMapping(adapter);
            mappingFor(scope).put(transferableId, mapping);
        }

        return transferableId;
    }

    private Map<String, TransientRootAdapterMapping> mappingFor(final Scope scope) {
        if (scope == Scope.REQUEST) {
            return requestTransients;
        }
        if (scope == Scope.INTERACTION || scope == Scope.SESSION) {
            return sessionTransients;
        }
        throw new ScimpiException("Can't hold globally transient object");
    }



    ////////////////////////////////////////////////////
    // mappedTransientObject  (lookup)
    ////////////////////////////////////////////////////

    @Override
    public ObjectAdapter mappedTransientObject(final String jsonObjectData) {
        final String objectData = jsonObjectData; // StringEscapeUtils.unescapeHtml(data);
        if(LOG.isDebugEnabled()) {
            LOG.debug("data" + objectData);
        }

        try {
            final JSONObject jsonObject = new JSONObject(objectData);
            return restoreTransientObject(jsonObject);
        } catch (final JSONException e) {
            throw new ScimpiException("Problem reading data: " + jsonObjectData, e);
        }
    }

    private ObjectAdapter restoreTransientObject(final JSONObject jsonObject) throws JSONException {

        final ObjectAdapter adapter = getAdapter(jsonObject);

        //final String objectType = jsonObject.getString("_objectType");
        //final ObjectSpecification specification = getSpecificationLoader().lookupByObjectType(objectType);
        final ObjectSpecification specification = adapter.getSpecification();

        for (final ObjectAssociation association : specification.getAssociations(Contributed.EXCLUDED)) {
            final String fieldName = association.getId();

            final Object fieldValue = jsonObject.has(fieldName) ? jsonObject.get(fieldName) : null;

            if (association.getSpecification().isEncodeable()) {
                if (fieldValue == null) {
                    ((OneToOneAssociation) association).initAssociation(adapter, null);
                } else {
                    final EncodableFacet encodeableFacet = association.getSpecification().getFacet(EncodableFacet.class);
                    final ObjectAdapter fromEncodedString = encodeableFacet.fromEncodedString((String) fieldValue);
                    ((OneToOneAssociation) association).initAssociation(adapter, fromEncodedString);
                }
            } else if (association instanceof OneToManyAssociation) {
                final JSONArray collection = (JSONArray) fieldValue;
                for (int i = 0; i < collection.length(); i++) {
                    final JSONObject jsonElement = (JSONObject) collection.get(i);
                    final ObjectAdapter objectToAdd = restoreTransientObject(jsonElement);
                    ((OneToManyAssociation) association).addElement(adapter, objectToAdd);
                }

                /*
                 * CollectionFacet facet =
                 * fieldValue.getSpecification().getFacet
                 * (CollectionFacet.class); for (ObjectAdapter element :
                 * facet.iterable(fieldValue)) {
                 * collection.add(saveData(element, savedObject)); }
                 * data.put(fieldName, collection);
                 */
            } else {
                if (fieldValue == null) {
                    ((OneToOneAssociation) association).initAssociation(adapter, null);
                } else {
                    if (fieldValue instanceof JSONObject) {
                        final ObjectAdapter fieldObject = restoreTransientObject((JSONObject) fieldValue);
                        ((OneToOneAssociation) association).initAssociation(adapter, fieldObject);
                    } else {
                        final ObjectAdapter field = mappedObject((String) fieldValue);
                        ((OneToOneAssociation) association).initAssociation(adapter, field);
                    }
                }
            }
        }
        return adapter;
    }

    private ObjectAdapter getAdapter(final JSONObject jsonObject) throws JSONException {

        //final String objectType = jsonObject.getString("_objectType");
        //final String id = jsonObject.getString("_id");
        //final ObjectSpecification objectSpec = getSpecificationLoader().lookupByObjectType(objectType);

        final String oidStr = jsonObject.getString("_oid");
        final TypedOid typedOid = getOidMarshaller().unmarshal(oidStr, TypedOid.class);

        if(!typedOid.isTransient()) {
            return getAdapterManager().adapterFor(typedOid);
        } else {
            return mappedObject(oidStr);
        }

//        if (objectSpec.isParented() && !objectSpec.isParentedOrFreeCollection()) {
//            final String[] split = id.split("@");
//            final String parentOidStr = split[0];
//            final String aggregatedLocalId = split[1];
//
//            RootOid parentOid;
//            if(RootOid.class.isAssignableFrom(oidType)) {
//                parentOid = getOidStringifier().deString(parentOidStr);
//            } else if (RootOidDefault.class.isAssignableFrom(oidType)) {
//                parentOid = RootOidDefault.createTransient(objectType, parentOidStr);
//            } else {
//                // REVIEW: for now, don't support holding references to aggregates whose parent is also an aggregate
//                throw new ScimpiException("Unsupported OID type " + oidType);
//            }
//
//            final AggregatedOid oid = new AggregatedOid(objectType, parentOid, aggregatedLocalId);
//            return getPersistenceSession().recreateAdapter(oid, objectSpec);
//        } else {
//            return mappedObject("T" + objectType + "@" + id); // yuk!
//        }
    }



    ////////////////////////////////////////////////////
    // mappedObject  (lookup - either persistent or transient)
    ////////////////////////////////////////////////////

    @Override
    public ObjectAdapter mappedObject(final String oidStr) {

        final TypedOid typedOid = getOidMarshaller().unmarshal(oidStr, TypedOid.class);


//        final char type = oidStr.charAt(0);
//
//        // Pdom.todo.ToDoItem@OID:TODO:6
//        final String[] split = oidStr.split("@");
//        final String oidData = split[1];
//        final String[] oidDataArray = oidData.split(":");
//        final String objectType = oidDataArray[1];
//        final String aggregatedId = split.length > 2?split[2]:null;
//
//        final ObjectSpecification spec = getSpecificationLoader().lookupByObjectType(objectType);

        //if ((type == 'T')) {
        if (typedOid.isTransient()) {

            TransientRootAdapterMapping mapping = sessionTransients.get(oidStr);
            if (mapping == null) {
                mapping = requestTransients.get(oidStr);
            }
            if (mapping == null) {

                // create as a (transient) root adapter
                // Oid oid = deString(objectType, oidData, State.TRANSIENT);
                //return getPersistenceSession().recreateAdapter(oid, pojo);

                return getAdapterManager().adapterFor(typedOid);
            }

            final ObjectAdapter mappedTransientObject = mapping.getObject();
            if(LOG.isDebugEnabled()) {
                LOG.debug("retrieved " + mappedTransientObject.getOid() + " for " + oidStr);
            }

            return mappedTransientObject;
        }

        try {
            //LOG.debug("decoding " + oidData);

            //if (aggregatedId != null) {
            if(typedOid instanceof AggregatedOid) {

//              final RootOid parentOid = deString(objectType, oidData, State.PERSISTENT);
//              Oid aggregatedOid = new AggregatedOid(objectType, parentOid, aggregatedId);

                AggregatedOid aggregatedOid = (AggregatedOid) typedOid;
                final TypedOid parentOid = aggregatedOid.getParentOid();

                getPersistenceSession().loadObject(parentOid);
                return getAdapterManager().getAdapterFor(aggregatedOid);
            }

//          RootOid oid = deString(objectType, oidData, State.PERSISTENT);
//          return getPersistenceSession().loadObject(oid);

            return getPersistenceSession().loadObject(typedOid);

        } catch (final SecurityException e) {
            throw new IsisException(e);
        }
    }


    ///////////////////////////////////////////////////////
    // reloadIdentityMap  (reloads the session transients)
    ///////////////////////////////////////////////////////

    @Override
    public void reloadIdentityMap() {
        final Iterator<TransientRootAdapterMapping> mappings = sessionTransients.values().iterator();
        while (mappings.hasNext()) {
            final TransientRootAdapterMapping mapping = mappings.next();
            mapping.reload();
        }
    }


    ////////////////////////////////////////////////////
    // unmapObject  (unmaps the transients)
    ////////////////////////////////////////////////////

    @Override
    public void unmapObject(final ObjectAdapter object, final Scope scope) {
        sessionTransients.remove(object.getOid());
        requestTransients.remove(object.getOid());
    }


    ///////////////////////////////////////
    // helpers
    ///////////////////////////////////////

//    enum State { TRANSIENT, PERSISTENT }

//    private RootOid deString(String objectType, final String oidData, State stateHint) {
//        if(RootOid.class.isAssignableFrom(oidType)) {
//            return getOidStringifier().deString(oidData);
//        } else {
//            throw new ScimpiException("Unsupported OID type " + oidType);
//        }
//    }


//    private String enString(RootOid ows) {
//        return getOidStringifier().enString(ows);
//    }

//    private String enString(final Oid parentOid, final String aggregatedId) {
//        return enString(parentOid) + "@" + aggregatedId;
//    }

//    private String enString(final Oid oid) {
//        final String parentOidStr;
//        if(oid instanceof RootOid) {
//            RootOid ows = (RootOid) oid;
//            parentOidStr = enString(ows);
//        } else if (oid instanceof RootOidDefault) {
//            final RootOidDefault parentSerialOid = (RootOidDefault) oid;
//            parentOidStr = parentSerialOid.getIdentifier();
//        } else {
//            throw new ScimpiException("Unsupported OID type " + oid);
//        }
//        return parentOidStr;
//    }



    /////////////////////////////////////////////////////////////////////////
    // debugging
    /////////////////////////////////////////////////////////////////////////

    @Override
    public void append(final DebugBuilder debug) {
        append(debug, requestTransients, "request");
        append(debug, sessionTransients, "session");
    }

    private void append(final DebugBuilder debug, final Map<String, TransientRootAdapterMapping> transients, final String type) {
        final Iterator<String> ids = new HashSet<String>(transients.keySet()).iterator();
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


    ///////////////////////////////////////
    // from context
    ///////////////////////////////////////

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }

}
