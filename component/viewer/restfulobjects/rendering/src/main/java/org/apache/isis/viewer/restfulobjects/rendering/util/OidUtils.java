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
package org.apache.isis.viewer.restfulobjects.rendering.util;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;

public final class OidUtils {

    private OidUtils() {
    }

    public static String getDomainType(final ObjectAdapter objectAdapter) {
        Oid oid = objectAdapter.getOid();
        if (oid == null || !(oid instanceof TypedOid)) {
            return null;
        }
        TypedOid typedOid = (TypedOid) oid;
        return typedOid.getObjectSpecId().asString();
    }

    public static String getInstanceId(final RendererContext renderContext, final ObjectAdapter objectAdapter) {
        String oidStr = getOidStr(renderContext, objectAdapter);
        // REVIEW: it's a bit hokey to join these together just to split them out again.
        return oidStr != null ? getOidMarshaller().splitInstanceId(oidStr): null;
    }

    
    public static String getOidStr(final RendererContext renderContext, final ObjectAdapter objectAdapter) {
        final Oid oid = objectAdapter.getOid();
        if (!(oid instanceof RootOid)) {
            throw new IllegalArgumentException("objectAdapter must be a root adapter");
        }
        return oid != null ? oid.enStringNoVersion(getOidMarshaller()) : null;
    }

    private static OidMarshaller getOidMarshaller() {
		return new OidMarshaller();
	}

    
}
