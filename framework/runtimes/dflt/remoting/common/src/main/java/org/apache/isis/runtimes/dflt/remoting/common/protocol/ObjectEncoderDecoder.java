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

package org.apache.isis.runtimes.dflt.remoting.common.protocol;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.remoting.common.data.Data;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.EncodableObjectData;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.IdentityData;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.ObjectData;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.ReferenceData;
import org.apache.isis.runtimes.dflt.remoting.common.data.query.PersistenceQueryData;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.AuthorizationResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ExecuteClientActionResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ExecuteServerActionResponse;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.KnownObjectsRequest;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;

public interface ObjectEncoderDecoder {

    // ////////////////////////////////////////////////
    // Authorization
    // ////////////////////////////////////////////////

    AuthorizationResponse encodeAuthorizeResponse(boolean allowed);

    // ////////////////////////////////////////////////
    // Field Order
    // ////////////////////////////////////////////////

    /**
     * Returns the agreed order to transfer fields within data objects. Both
     * remote parties need to process the fields in the same order, this is that
     * order.
     */
    ObjectAssociation[] getFieldOrder(ObjectSpecification specification);

    // ////////////////////////////////////////////////
    // Identity
    // ////////////////////////////////////////////////

    IdentityData encodeIdentityData(ObjectAdapter object);

    // ////////////////////////////////////////////////
    // Resolves
    // ////////////////////////////////////////////////

    Data encodeForResolveField(ObjectAdapter targetAdapter, String fieldName);

    // ////////////////////////////////////////////////
    // Actions & Parameters
    // ////////////////////////////////////////////////

    ReferenceData encodeActionTarget(ObjectAdapter targetAdapter, KnownObjectsRequest knownObjects);

    Data[] encodeActionParameters(ObjectSpecification[] parameterTypes, ObjectAdapter[] parameterAdapters, KnownObjectsRequest knownObjects);

    ExecuteServerActionResponse encodeServerActionResult(ObjectAdapter resultAdapter, ObjectData[] updatedData, ReferenceData[] disposedData, ObjectData persistedTargetData, ObjectData[] persistedParameterData, String[] messages, String[] warnings);

    ExecuteClientActionResponse encodeClientActionResult(ReferenceData[] madePersistent, Version[] changedVersion, ObjectData[] updates);

    // ////////////////////////////////////////////////
    // Graphs
    // ////////////////////////////////////////////////

    /**
     * Creates an {@link ObjectData} that contains all the data for all the
     * transient objects in the specified transient object.
     * 
     * <p>
     * For any referenced persistent object in the graph, only the reference is
     * passed across.
     */
    ObjectData encodeMakePersistentGraph(ObjectAdapter adapter, KnownObjectsRequest knownObjects);

    ObjectData encodeGraphForChangedObject(ObjectAdapter adapter, KnownObjectsRequest knownObjects);

    /**
     * Creates a graph of ReferenceData objects (mirroring the graph of
     * transient objects) to transfer the OIDs and Versions for each object that
     * was made persistent during the makePersistent call.
     */
    ObjectData encodeMadePersistentGraph(ObjectData originalData, ObjectAdapter adapter);

    /**
     * Creates an ObjectData that contains all the data for all the objects in
     * the graph. This allows the client to receive all data it might need
     * without having to return to the server to get referenced objects.
     */
    ObjectData encodeCompletePersistentGraph(ObjectAdapter object);

    // ////////////////////////////////////////////////
    // Value
    // ////////////////////////////////////////////////

    EncodableObjectData encodeAsValue(ObjectAdapter value);

    // ////////////////////////////////////////////////
    // Update
    // ////////////////////////////////////////////////

    /**
     * Creates an {@link ObjectData} that contains the data for the specified
     * object, but not the data for any referenced objects.
     * 
     * <p>
     * For each referenced object only the reference is passed across.
     */
    ObjectData encodeForUpdate(ObjectAdapter object);

    // ////////////////////////////////////////////////
    // decode
    // ////////////////////////////////////////////////

    ObjectAdapter decode(Data data);

    void decode(ObjectData[] dataArray);

    ObjectAdapter decode(Data data, KnownObjectsRequest knownObjects);

    // ////////////////////////////////////////////////
    // PersistenceQuery
    // ////////////////////////////////////////////////

    PersistenceQueryData encodePersistenceQuery(PersistenceQuery persistenceQuery);

    PersistenceQuery decodePersistenceQuery(PersistenceQueryData persistenceQueryData);

    // ////////////////////////////////////////////////
    // makePersistent
    // ////////////////////////////////////////////////

    void madePersistent(ObjectAdapter target, ObjectData persistedTarget);

}
