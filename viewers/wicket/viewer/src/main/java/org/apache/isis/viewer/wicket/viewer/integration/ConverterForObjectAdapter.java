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

package org.apache.isis.viewer.wicket.viewer.integration;

import java.util.Locale;

import javax.inject.Inject;

import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

/**
 * Implementation of a Wicket {@link IConverter} for {@link ManagedObject}s,
 * converting to-and-from their {@link Oid}'s string representation.
 */
public class ConverterForObjectAdapter implements IConverter<ManagedObject> {

    private static final long serialVersionUID = 1L;

    @Inject private transient ObjectManager objectManager;
    
    /**
     * Converts string representation of {@link Oid} to
     * {@link ManagedObject}.
     */
    @Override
    public ManagedObject convertToObject(final String value, final Locale locale) {
        val rootOid = RootOid.deStringEncoded(value);
        val objectSpecId = rootOid.getObjectSpecId(); 
        val spec = objectManager.getMetaModelContext()
                .getSpecificationLoader()
                .lookupBySpecIdElseLoad(objectSpecId);
        
        val objectLoadRequest = ObjectLoader.Request.of(spec, rootOid.getIdentifier());
        
        return objectManager.loadObject(objectLoadRequest);
        
        // legacy of
        //return getPersistenceSession().adapterFor(rootOid);
    }

    /**
     * Converts {@link ManagedObject} to string representation of {@link Oid}.
     */
    @Override
    public String convertToString(final ManagedObject adapter, final Locale locale) {
        
        if(!ManagedObject.isBookmarkable(adapter)) {
            // eg. values don't have an Oid
            return null;
        }
        
        val rootOid = ManagedObject._identify(adapter);
        
        return rootOid!=null
                ? rootOid.enString()
                        : null;
    }
    

}
