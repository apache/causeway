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

package org.apache.isis.viewer.wicket.viewer.services.mementos;

import java.io.Serializable;

import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

import lombok.val;

final class StandaloneData extends Data {

    private static final long serialVersionUID = 1L;

    private String objectAsEncodedString;
    private Serializable objectAsSerializable;

    public StandaloneData(RootOid rootOid, ManagedObject adapter) {
        super(rootOid);

        final Object object = adapter.getPojo();
        if (object instanceof Serializable) {
            this.objectAsSerializable = (Serializable) object;
            return;
        }

        val encodeableFacet = adapter.getSpecification().getFacet(EncodableFacet.class);
        if (encodeableFacet != null) {
            this.objectAsEncodedString = encodeableFacet.toEncodedString(adapter);
            return;
        }

        throw new IllegalArgumentException("Object wrapped by standalone adapter is not serializable and its specificatoin does not have an EncodeableFacet");
    }

    public ManagedObject getAdapter(
            ObjectAdapterProvider objectAdapterProvider,
            SpecificationLoader specificationLoader) {
        
        if (objectAsSerializable != null) {
            return objectAdapterProvider.adapterFor(objectAsSerializable);
        } else {
            val spec = specificationLoader.lookupBySpecIdElseLoad(getObjectSpecId());
            val encodeableFacet = spec.getFacet(EncodableFacet.class);
            return encodeableFacet.fromEncodedString(objectAsEncodedString);
        }
    }

}
