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
package org.apache.isis.viewer.restfulobjects.rendering;

import org.apache.isis.commons.collections.Bin;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.ioc.ManagedBeanAdapter;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.exceptions.persistence.ObjectNotFoundException;
import org.apache.isis.metamodel.exceptions.persistence.PojoRecreationException;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;

import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;

import lombok.val;

final class OidUtils {

    private OidUtils() {
    }

    /**
     * @return {@code null} if not persistent and not a view model.
     */
    public static ObjectAdapter getObjectAdapterElseNull(
            final RendererContext rendererContext,
            final String domainType, 
            final String instanceIdEncoded) {

        final String instanceIdUnencoded = UrlDecoderUtils.urlDecode(instanceIdEncoded);
        String oidStrUnencoded = Oid.marshaller().joinAsOid(domainType, instanceIdUnencoded);
        return getObjectAdapter(rendererContext, oidStrUnencoded);
    }

    /**
     * see {@link #getObjectAdapterElseNull(org.apache.isis.viewer.restfulobjects.rendering.RendererContext, String, String)}
     */
    public static ObjectAdapter getObjectAdapterElseNull(
            final RendererContext rendererContext,
            final String oidStrEncoded) {

        String oidStrUnencoded = UrlDecoderUtils.urlDecode(oidStrEncoded);
        return getObjectAdapter(rendererContext, oidStrUnencoded);
    }

    // -- HELPER

    private static ObjectAdapter getObjectAdapter(
            final RendererContext rendererContext,
            final String oidStrUnencoded) {

        final RootOid rootOid = RootOid.deString(oidStrUnencoded);
        final Object domainObject = domainObjectForAny(rendererContext, rootOid);

        return rendererContext.adapterOfPojo(domainObject);
    }

    private static Object domainObjectForAny(final RendererContext rendererContext, final RootOid rootOid) {

        val specId = rootOid.getObjectSpecId();
        val spec = MetaModelContext.current().getSpecificationLoader().lookupBySpecIdElseLoad(specId);
        if(spec == null) {
            // eg "NONEXISTENT:123"
            return null;
        }

        //TODO[2158] remove eventually
        _Assert.assertEquals("expected same", 
                spec.getBeanSort().isViewModel(), 
                spec.containsFacet(ViewModelFacet.class));
        
        if(spec.getBeanSort().isViewModel()) {

            try {
                val fixedRootOid = ensureConsistentOidState(rootOid);
                val adapter = rendererContext.adapterOfPojo(fixedRootOid);

                val pojo = mapIfPresentElse(adapter, ObjectAdapter::getPojo, null);
                return pojo;

            } catch(ObjectNotFoundException | PojoRecreationException ex) {
                return null;
            }
        } else if(spec.getBeanSort().isEntity()){
            try {
                val pojo = rendererContext.fetchPersistentPojoInTransaction(rootOid);
                //TODO[ISIS-1976] changed behavior: predicate was objectAdapter.isTransient();
                return rendererContext.stateOf(pojo).isDetached()
                        ? null 
                                : pojo;
            } catch(ObjectNotFoundException ex) {
                return null;
            }
        } else if(spec.getBeanSort().isManagedBean()){
            
            val servicePojo = rendererContext.getServiceRegistry()
                    .lookupRegisteredBeanById(spec.getSpecId().asString())
                    .map(ManagedBeanAdapter::getInstance)
                    .flatMap(Bin::getFirst)
                    .orElse(null);
            
            if(servicePojo!=null) {
                return servicePojo;
            }
            // fall through and throw
            
        } 
        throw _Exceptions.unrecoverableFormatted(
                "unhandled request for a %s having rootId %s",
                spec,
                rootOid);
        
    }

    private static RootOid ensureConsistentOidState(RootOid rootOid) {
        // this is a hack; the RO viewer when rendering the URL for the view model loses the "view model" indicator
        // ("*") from the specId, meaning that the marshalling logic above in RootOidDefault.deString() creates an
        // oid in the wrong state.  The code below checks for this and recreates the oid with the current state of 'view model'
        if(!rootOid.isViewModel()) {
            return Oid.Factory.viewmodelOf(rootOid.getObjectSpecId(), rootOid.getIdentifier());
        }
        return rootOid;
    }

}
