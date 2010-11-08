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

package org.apache.isis.remoting.protocol.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.commons.factory.InstanceFactory;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.util.CollectionFacetUtils;
import org.apache.isis.remoting.IsisRemoteException;
import org.apache.isis.remoting.client.facets.ActionInvocationFacetWrapProxy;
import org.apache.isis.remoting.client.facets.PropertySetterFacetWrapProxy;
import org.apache.isis.remoting.client.persistence.ClientSideTransactionManager;
import org.apache.isis.remoting.client.persistence.PersistenceSessionProxy;
import org.apache.isis.remoting.data.Data;
import org.apache.isis.remoting.data.DataFactory;
import org.apache.isis.remoting.data.DataFactoryDefault;
import org.apache.isis.remoting.data.common.CollectionData;
import org.apache.isis.remoting.data.common.EncodableObjectData;
import org.apache.isis.remoting.data.common.IdentityData;
import org.apache.isis.remoting.data.common.NullData;
import org.apache.isis.remoting.data.common.ObjectData;
import org.apache.isis.remoting.data.common.ReferenceData;
import org.apache.isis.remoting.data.query.PersistenceQueryData;
import org.apache.isis.remoting.exchange.AuthorizationResponse;
import org.apache.isis.remoting.exchange.ClearAssociationRequest;
import org.apache.isis.remoting.exchange.ClearValueRequest;
import org.apache.isis.remoting.exchange.ExecuteClientActionRequest;
import org.apache.isis.remoting.exchange.ExecuteClientActionResponse;
import org.apache.isis.remoting.exchange.ExecuteServerActionRequest;
import org.apache.isis.remoting.exchange.ExecuteServerActionResponse;
import org.apache.isis.remoting.exchange.FindInstancesRequest;
import org.apache.isis.remoting.exchange.GetObjectRequest;
import org.apache.isis.remoting.exchange.KnownObjectsRequest;
import org.apache.isis.remoting.exchange.ResolveFieldRequest;
import org.apache.isis.remoting.exchange.ResolveObjectRequest;
import org.apache.isis.remoting.exchange.SetAssociationRequest;
import org.apache.isis.remoting.exchange.SetValueRequest;
import org.apache.isis.remoting.facade.impl.ServerFacadeImpl;
import org.apache.isis.remoting.protocol.ObjectEncoderDecoder;
import org.apache.isis.remoting.protocol.PersistenceQueryEncoder;
import org.apache.isis.remoting.protocol.ProtocolConstants;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistorUtil;
import org.apache.isis.runtime.persistence.query.PersistenceQuery;
import org.apache.log4j.Logger;

public class ObjectEncoderDecoderDefault implements ObjectEncoderDecoder {

    private final static Logger LOG = Logger.getLogger(ObjectEncoderDecoderDefault.class);

    public static final int DEFAULT_CLIENT_SIDE_ADD_OBJECT_GRAPH_DEPTH = 1;
    public static final int DEFAULT_CLIENT_SIDE_UPDATE_OBJECT_GRAPH_DEPTH = 1;
    public static final int DEFAULT_CLIENT_SIDE_ACTION_TARGET_GRAPH_DEPTH = 0;
    public static final int DEFAULT_CLIENT_SIDE_ACTION_PARAMETER_GRAPH_DEPTH = 0;

    public static final int DEFAULT_SERVER_SIDE_RETRIEVED_OBJECT_GRAPH_DEPTH = 100;
    public static final int DEFAULT_SERVER_SIDE_TOUCHED_OBJECT_GRAPH_DEPTH = 1;

    private final ObjectSerializer serializer;
    private final ObjectDeserializer deserializer;
    private final FieldOrderCache fieldOrderCache;
    private final DataFactory dataFactory;

    private final Map<Class<?>, PersistenceQueryEncoder> persistenceEncoderByClass =
        new HashMap<Class<?>, PersistenceQueryEncoder>();

    private final int clientSideAddGraphDepth = DEFAULT_CLIENT_SIDE_ADD_OBJECT_GRAPH_DEPTH;
    private final int clientSideUpdateGraphDepth = DEFAULT_CLIENT_SIDE_UPDATE_OBJECT_GRAPH_DEPTH;
    private final int clientSideActionTargetRemotelyGraphDepth = DEFAULT_CLIENT_SIDE_ACTION_TARGET_GRAPH_DEPTH;
    private final int clientSideActionParameterGraphDepth = DEFAULT_CLIENT_SIDE_ACTION_PARAMETER_GRAPH_DEPTH;

    private final int serverSideTouchedObjectGraphDepth = DEFAULT_SERVER_SIDE_TOUCHED_OBJECT_GRAPH_DEPTH;
    private final int serverSideRetrievedObjectGraphDepth = DEFAULT_SERVER_SIDE_RETRIEVED_OBJECT_GRAPH_DEPTH;

    /**
     * Factory method.
     */
    public static ObjectEncoderDecoderDefault create(final IsisConfiguration configuration) {

        ObjectEncoderDecoderDefault encoderDecoder = new ObjectEncoderDecoderDefault();
        addPersistenceEncoders(configuration, encoderDecoder, ProtocolConstants.ENCODER_CLASS_NAME_LIST);
        addPersistenceEncoders(configuration, encoderDecoder,
            ProtocolConstants.ENCODER_CLASS_NAME_LIST_DEPRECATED);
        return encoderDecoder;
    }

    private static void addPersistenceEncoders(final IsisConfiguration configuration,
        final ObjectEncoderDecoderDefault encoder, String encoderClassNameList) {
        String[] encoders = configuration.getList(encoderClassNameList);
        for (int i = 0; i < encoders.length; i++) {
            final PersistenceQueryEncoder encoding =
                InstanceFactory.createInstance(encoders[i], PersistenceQueryEncoder.class);
            encoder.addPersistenceQueryEncoder(encoding);
        }
    }

    /**
     * Package-level visibility (for tests to use only)
     */
    public ObjectEncoderDecoderDefault() {
        this.fieldOrderCache = new FieldOrderCache();
        this.dataFactory = new DataFactoryDefault();
        this.serializer = new ObjectSerializer(dataFactory, fieldOrderCache);
        this.deserializer = new ObjectDeserializer(fieldOrderCache);

        addPersistenceQueryEncoder(new PersistenceQueryFindAllInstancesEncoder());
        addPersistenceQueryEncoder(new PersistenceQueryFindByTitleEncoder());
        addPersistenceQueryEncoder(new PersistenceQueryFindByPatternEncoder());
        addPersistenceQueryEncoder(new PersistenceQueryFindUsingApplibQueryDefaultEncoder());
        addPersistenceQueryEncoder(new PersistenceQueryFindUsingApplibQuerySerializableEncoder());

        // TODO: look up overrides of depths from Configuration.
    }

    public void addPersistenceQueryEncoder(final PersistenceQueryEncoder encoder) {
        encoder.setObjectEncoder(this);
        persistenceEncoderByClass.put(encoder.getPersistenceQueryClass(), encoder);
    }

    // /////////////////////////////////////////////////////////
    // called both client- and server-side only
    // /////////////////////////////////////////////////////////

    /**
     * Creates a ReferenceData that contains the type, version and OID for the specified object. This can only be used
     * for persistent objects.
     * 
     * <p>
     * Called both client and server-side, in multiple locations.
     */
    @Override
    public final IdentityData encodeIdentityData(final ObjectAdapter object) {
        Assert.assertNotNull("OID needed for reference", object, object.getOid());
        return dataFactory.createIdentityData(object.getSpecification().getFullName(), object.getOid(),
            object.getVersion());
    }

    // /////////////////////////////////////////////////////////
    // client-side encoding
    // /////////////////////////////////////////////////////////

    /**
     * Called client-side only:
     * <ul>
     * <li>by {@link ClientSideTransactionManager#endTransaction()}
     * </ul>
     */
    @Override
    public ObjectData encodeMakePersistentGraph(final ObjectAdapter adapter, final KnownObjectsRequest knownObjects) {
        Assert.assertTrue("transient", adapter.isTransient());
        return (ObjectData) encode(adapter, clientSideAddGraphDepth, knownObjects);
    }

    /**
     * Called client-side only:
     * <ul>
     * <li>by {@link ClientSideTransactionManager#endTransaction()}
     * </ul>
     */
    @Override
    public ObjectData encodeGraphForChangedObject(final ObjectAdapter object, final KnownObjectsRequest knownObjects) {
        return (ObjectData) encode(object, clientSideUpdateGraphDepth, knownObjects);
    }

    /**
     * Called client-side only:
     * <ul>
     * <li>by {@link PropertySetterFacetWrapProxy#setProperty(ObjectAdapter, ObjectAdapter)}
     * </ul>
     */
    @Override
    public EncodableObjectData encodeAsValue(final ObjectAdapter value) {
        return serializer.serializeEncodeable(value);
    }

    /**
     * Called client-side only:
     * <ul>
     * <li>by {@link ActionInvocationFacetWrapProxy#invoke(ObjectAdapter, ObjectAdapter[])} (calling remotely)
     * </ul>
     */
    @Override
    public ReferenceData encodeActionTarget(final ObjectAdapter target, final KnownObjectsRequest knownObjects) {
        return serializer.serializeAdapter(target, clientSideActionTargetRemotelyGraphDepth, knownObjects);
    }

    /**
     * Called client-side only:
     * <ul>
     * <li>by {@link ActionInvocationFacetWrapProxy#invoke(ObjectAdapter, ObjectAdapter[])}
     * <li>by {@link PersistenceQueryFindByPatternEncoder#encode(PersistenceQuery)}
     * <li>by hibernate's equivalent encoder
     * </ul>
     */
    @Override
    public final Data[] encodeActionParameters(final ObjectSpecification[] parameterTypes,
        final ObjectAdapter[] parameters, final KnownObjectsRequest knownObjects) {
        final Data parameterData[] = new Data[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            final ObjectAdapter parameter = parameters[i];
            final String type = parameterTypes[i].getFullName();
            parameterData[i] = createParameter(type, parameter, knownObjects);
        }
        return parameterData;
    }

    /**
     * Called client-side only:
     * <ul>
     * <li>by {@link PersistenceSessionProxy#findInstances(PersistenceQuery)}
     * </ul>
     */
    @Override
    public PersistenceQueryData encodePersistenceQuery(final PersistenceQuery criteria) {
        final PersistenceQueryEncoder strategy = findPersistenceQueryEncoder(criteria.getClass());
        return strategy.encode(criteria);
    }

    // /////////////////////////////////////////////////////////
    // client-side decoding
    // /////////////////////////////////////////////////////////

    /**
     * Called client-side only:
     * <ul>
     * <li>by {@link ActionInvocationFacetWrapProxy#invoke(ObjectAdapter, ObjectAdapter[])}
     * </ul>
     */
    @Override
    public void madePersistent(final ObjectAdapter target, final ObjectData persistedTarget) {
        deserializer.madePersistent(target, persistedTarget);
    }

    /**
     * Called client-side only, in multiple locations.
     */
    @Override
    public ObjectAdapter decode(final Data data) {
        return deserializer.deserialize(data);
    }

    @Override
    public void decode(final ObjectData[] dataArray) {
        for (int i = 0; i < dataArray.length; i++) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("update " + dataArray[i].getOid());
            }
            this.decode(dataArray[i]);
        }
    }

    // /////////////////////////////////////////////////////////
    // server-side decoding
    // /////////////////////////////////////////////////////////

    /**
     * Called server-side only:
     * <ul>
     * <li>by {@link ServerFacadeImpl#executeClientAction(ExecuteClientActionRequest)}
     * <li>by {@link ServerFacadeImpl#executeServerAction(ExecuteServerActionRequest) </ul>
     */
    @Override
    public ObjectAdapter decode(final Data data, final KnownObjectsRequest knownObjects) {
        return deserializer.deserialize(data, knownObjects);
    }

    /**
     * Called server-side only:
     * <ul>
     * <li>by {@link ServerFacadeImpl#findInstances(FindInstancesRequest) </ul>
     */
    @Override
    public PersistenceQuery decodePersistenceQuery(final PersistenceQueryData persistenceQueryData) {
        final Class<?> criteriaClass = persistenceQueryData.getPersistenceQueryClass();
        final PersistenceQueryEncoder encoderDecoder = findPersistenceQueryEncoder(criteriaClass);
        return encoderDecoder.decode(persistenceQueryData);
    }

    private PersistenceQueryEncoder findPersistenceQueryEncoder(final Class<?> persistenceQueryClass) {
        final PersistenceQueryEncoder encoder = persistenceEncoderByClass.get(persistenceQueryClass);
        if (encoder == null) {
            throw new IsisRemoteException("No encoder for " + persistenceQueryClass.getName());
        }
        return encoder;
    }

    // /////////////////////////////////////////////////////////
    // server-side encoding
    // /////////////////////////////////////////////////////////

    @Override
    public AuthorizationResponse encodeAuthorizeResponse(boolean authorized) {
        return new AuthorizationResponse(authorized);
    }

    /**
     * Called server-side only:
     * <ul>
     * <li>by {@link ServerFacadeImpl#executeClientAction(ExecuteClientActionRequest)}
     * </ul>
     */
    @Override
    public ExecuteClientActionResponse encodeClientActionResult(final ReferenceData[] madePersistent,
        final Version[] changedVersion, final ObjectData[] updates) {
        return new ExecuteClientActionResponse(madePersistent, changedVersion, updates);
    }

    /**
     * Encodes a complete set of data for the specified object.
     * 
     * <p>
     * Called server-side only, in several locations:
     * <ul>
     * <li>by {@link ServerFacadeImpl#findInstances(FindInstancesRequest)}
     * <li>by {@link ServerFacadeImpl#executeServerAction(ExecuteServerActionRequest)}
     * <li>by {@link ServerFacadeImpl#resolveImmediately(ResolveObjectRequest)}
     * </ul>
     */
    @Override
    public final ObjectData encodeCompletePersistentGraph(final ObjectAdapter object) {
        return encode(object, serverSideRetrievedObjectGraphDepth);
    }

    /**
     * Encodes a minimal set of data for the specified object.
     * 
     * <p>
     * Called server-side only:
     * <ul>
     * <li>by {@link ServerFacadeImpl#getObject(GetObjectRequest)}
     * <li>by {@link ServerFacadeImpl#clearAssociation(ClearAssociationRequest) <li><li> by
     * {@link ServerFacadeImpl#clearValue(ClearValueRequest) <li><li> by
     * {@link ServerFacadeImpl#executeClientAction(ExecuteClientActionRequest) <li><li> by
     * {@link ServerFacadeImpl#executeServerAction(ExecuteServerActionRequest) <li><li> by
     * {@link ServerFacadeImpl#setAssociation(SetAssociationRequest) <li><li> by
     * {@link ServerFacadeImpl#setValue(SetValueRequest) </ul>
     */
    @Override
    public ObjectData encodeForUpdate(final ObjectAdapter object) {
        final ResolveState resolveState = object.getResolveState();
        if (resolveState.isSerializing() || resolveState.isGhost()) {
            throw new IsisRemoteException("Illegal resolve state: " + object);
        }
        return encode(object, serverSideTouchedObjectGraphDepth);
    }

    /**
     * Called server-side only:
     * <ul>
     * <li>by {@link ServerFacadeImpl#resolveField(ResolveFieldRequest)}
     * </ul>
     */
    @Override
    public Data encodeForResolveField(final ObjectAdapter adapter, final String fieldName) {
        final Oid oid = adapter.getOid();
        final ObjectSpecification specification = adapter.getSpecification();
        final String type = specification.getFullName();
        final ResolveState resolveState = adapter.getResolveState();

        Data[] fieldContent;
        final ObjectAssociation[] fields = getFieldOrder(specification);
        fieldContent = new Data[fields.length];

        PersistorUtil.start(adapter, adapter.getResolveState().serializeFrom());
        final KnownObjectsRequest knownObjects = new KnownObjectsRequest();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getId().equals(fieldName)) {
                final ObjectAdapter field = fields[i].get(adapter);
                if (field == null) {
                    fieldContent[i] = dataFactory.createNullData(fields[i].getSpecification().getFullName());
                } else if (fields[i].getSpecification().isEncodeable()) {
                    fieldContent[i] = serializer.serializeEncodeable(field);
                } else if (fields[i].isOneToManyAssociation()) {
                    fieldContent[i] =
                        serializer.serializeCollection(field, serverSideRetrievedObjectGraphDepth, knownObjects);
                } else {
                    IsisContext.getPersistenceSession().resolveImmediately(field);
                    fieldContent[i] =
                        serializer.serializeAdapter(field, serverSideRetrievedObjectGraphDepth, knownObjects);
                }
                break;
            }
        }
        PersistorUtil.end(adapter);

        // TODO remove the fudge - needed as collections are part of parents, hence parent object gets set as
        // resolving (is not a ghost) yet it has no version number
        // return createObjectData(oid, type, fieldContent, resolveState.isResolved(),
        // !resolveState.isGhost(), object.getVersion());
        final ObjectData data =
            dataFactory.createObjectData(type, oid, resolveState.isResolved(), adapter.getVersion());
        data.setFieldContent(fieldContent);
        return data;
        // return createObjectData(oid, type, fieldContent, resolveState.isResolved(), object.getVersion());
    }

    /**
     * Called server-side only:
     * <ul>
     * <li>by {@link ServerFacadeImpl#executeServerAction(ExecuteServerActionRequest)}
     * </ul>
     */
    @Override
    public ObjectData encodeMadePersistentGraph(final ObjectData data, final ObjectAdapter object) {
        final Oid objectsOid = object.getOid();
        Assert.assertNotNull(objectsOid);
        if (objectsOid.hasPrevious()) {
            final Version version = object.getVersion();
            final String type = data.getType();
            final ObjectData persistedData = dataFactory.createObjectData(type, objectsOid, true, version);

            final Data[] allContents = data.getFieldContent();
            if (allContents != null) {
                final int contentLength = allContents.length;
                final Data persistentContents[] = new Data[contentLength];
                final ObjectAssociation[] fields = getFieldOrder(object.getSpecification());
                for (int i = 0; i < contentLength; i++) {
                    final Data fieldData = allContents[i];
                    if (fieldData instanceof NullData) {
                        persistentContents[i] = null;
                    } else if (fields[i].isOneToOneAssociation()) {
                        if (fieldData instanceof ObjectData) {
                            final ObjectAdapter fieldReference = fields[i].get(object);
                            persistentContents[i] = encodeMadePersistentGraph((ObjectData) fieldData, fieldReference);
                        } else {
                            persistentContents[i] = null;
                        }
                    } else if (fields[i].isOneToManyAssociation()) {
                        final ObjectAdapter fieldReference = fields[i].get(object);
                        persistentContents[i] =
                            createMadePersistentCollection((CollectionData) fieldData, fieldReference);
                    }
                }
                persistedData.setFieldContent(persistentContents);
            }

            return persistedData;
        } else {
            return null;
        }
    }

    private Data createMadePersistentCollection(final CollectionData collectionData, final ObjectAdapter collection) {
        final ReferenceData[] elementData = collectionData.getElements();
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
        final Iterator elements = facet.iterator(collection);
        for (int i = 0; i < elementData.length; i++) {
            final ObjectAdapter element = (ObjectAdapter) elements.next();
            final Oid oid = element.getOid();
            Assert.assertNotNull(oid);
            elementData[i] = encodeMadePersistentGraph((ObjectData) elementData[i], element);
        }
        return collectionData;
    }

    /**
     * Called server-side only:
     * <ul>
     * <li>by {@link ServerFacadeImpl#executeServerAction(ExecuteServerActionRequest)}
     * </ul>
     */
    @Override
    public ExecuteServerActionResponse encodeServerActionResult(final ObjectAdapter result,
        final ObjectData[] updatesData, final ReferenceData[] disposedData, final ObjectData persistedTargetData,
        final ObjectData[] persistedParametersData, final String[] messages, final String[] warnings) {
        Data resultData;
        if (result == null) {
            resultData = dataFactory.createNullData("");
        } else if (result.getSpecification().isCollection()) {
            resultData =
                serializer.serializeCollection(result, serverSideRetrievedObjectGraphDepth, new KnownObjectsRequest());
        } else if (result.getSpecification().isNotCollection()) {
            resultData = encodeCompletePersistentGraph(result);
        } else {
            throw new UnknownTypeException(result);
        }

        return new ExecuteServerActionResponse(resultData, updatesData, disposedData, persistedTargetData,
            persistedParametersData, messages, warnings);
    }

    /**
     * Called server-side only:
     * <ul>
     * <li>by {@link ServerFacadeImpl#resolveField(ResolveFieldRequest)}
     * <li>by {@link ServerFacadeImpl#executeServerAction(ExecuteServerActionRequest) </ul>
     */
    @Override
    public ObjectAssociation[] getFieldOrder(final ObjectSpecification specification) {
        return fieldOrderCache.getFields(specification);
    }

    // ///////////////////////////////////////////////////////////////
    // Helpers
    // ///////////////////////////////////////////////////////////////

    private final Data createParameter(final String type, final ObjectAdapter adapter,
        final KnownObjectsRequest knownObjects) {
        if (adapter == null) {
            return dataFactory.createNullData(type);
        }

        if (!adapter.getSpecification().isNotCollection()) {
            throw new UnknownTypeException(adapter.getSpecification());
        }

        if (adapter.getSpecification().isEncodeable()) {
            return serializer.serializeEncodeable(adapter);
        } else {
            return encode(adapter, clientSideActionParameterGraphDepth, knownObjects);
        }
    }

    private ObjectData encode(final ObjectAdapter adapter, int depth) {
        return (ObjectData) encode(adapter, depth, new KnownObjectsRequest());
    }

    private ReferenceData encode(final ObjectAdapter adapter, int depth, final KnownObjectsRequest knownObjects) {
        return serializer.serializeAdapter(adapter, depth, knownObjects);
    }

}
