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

import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ManagedObject;

import lombok.val;

@Deprecated // use framework API instead, needs migration
final class OidUtils {

    private OidUtils() {
    }

    /**
     * @return {@code null} if not persistent and not a view model.
     */
    public static ManagedObject getObjectAdapterElseNull(
            final IResourceContext resourceContext,
            final String domainType, 
            final String instanceIdEncoded) {

        final String instanceIdUnencoded = UrlDecoderUtils.urlDecode(instanceIdEncoded);
        String oidStrUnencoded = Oid.marshaller().joinAsOid(domainType, instanceIdUnencoded);
        return getObjectAdapter(resourceContext, oidStrUnencoded);
    }

    /**
     * see {@link #getObjectAdapterElseNull(org.apache.isis.viewer.restfulobjects.rendering.IResourceContext, String, String)}
     */
    public static ManagedObject getObjectAdapterElseNull(
            final IResourceContext resourceContext,
            final String oidStrEncoded) {

        String oidStrUnencoded = UrlDecoderUtils.urlDecode(oidStrEncoded);
        return getObjectAdapter(resourceContext, oidStrUnencoded);
    }

    // -- HELPER

    private static ManagedObject getObjectAdapter(
            final IResourceContext resourceContext,
            final String oidStrUnencoded) {

        val rootOid = RootOid.deString(oidStrUnencoded);
        return ManagedObject._adapterOfRootOid(resourceContext.getSpecificationLoader(), rootOid);
    }


}
