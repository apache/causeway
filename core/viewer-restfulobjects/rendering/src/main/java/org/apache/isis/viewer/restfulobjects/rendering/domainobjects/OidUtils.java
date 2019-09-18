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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;

final class OidUtils {

    private OidUtils() {
    }

    public static String getDomainType(final ObjectAdapter objectAdapter) {
        Oid oid = objectAdapter.getOid();
        if (oid == null || !(oid instanceof RootOid)) {
            return null;
        }
        RootOid rootOid = (RootOid) oid;
        return rootOid.getObjectSpecId().asString();
    }

    public static String getInstanceId(final ObjectAdapter objectAdapter) {
        String oidStr = getOidStr(objectAdapter);
        // REVIEW: it's a bit hokey to join these together just to split them out again.
        return oidStr != null ? Oid.unmarshaller().splitInstanceId(oidStr): null;
    }


    public static String getOidStr(final ObjectAdapter objectAdapter) {
        final Oid oid = objectAdapter.getOid();
        if (!(oid instanceof RootOid)) {
            throw new IllegalArgumentException("objectAdapter must be a root adapter");
        }
        return oid.enStringNoVersion();
    }


}
