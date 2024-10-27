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
package org.apache.causeway.persistence.jdo.datanucleus.entities;

import java.util.Optional;

import org.datanucleus.enhancement.Persistable;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * JDO distinguishes between DETACHED and HOLLOW, where we are (historically) using HOLLOW,
 * by virtue of {@code datanucleus.detachAllOnCommit=false}.
 * <p>
 * HOLLOW is more lightweight, but does not offer any built-in means to recover an OID,
 * hence we introduced this helper.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DnOidStoreAndRecoverHelper {

    public static DnOidStoreAndRecoverHelper forEntity(final Persistable entityPojo) {
        return new DnOidStoreAndRecoverHelper(entityPojo);
    }

    final @NonNull Persistable entityPojo;

    @SneakyThrows
    public void storeOid(final String oid) {
        entityPojo.dnReplaceStateManager(new DnStateManagerForHollow(oid));
        entityPojo.dnReplaceFlags(); // sets dnFlags to 0
    }

    @SneakyThrows
    public Optional<String> recoverOid() {
        var sm = entityPojo.dnGetStateManager();
        if(sm instanceof DnStateManagerForHollow) {
            var oidStringified = ((DnStateManagerForHollow)sm).oidStringified;
            return Optional.of(oidStringified);
        }
        return Optional.empty();
    }

}
