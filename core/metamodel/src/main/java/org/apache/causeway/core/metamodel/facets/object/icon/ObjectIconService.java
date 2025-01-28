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
package org.apache.causeway.core.metamodel.facets.object.icon;

import org.springframework.lang.Nullable;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import org.jspecify.annotations.NonNull;

/**
 * Creates {@link ObjectIcon}(s), which are class-path resource references.
 * <p>
 * Clients should not use this service directly. Either use
 * {@link ManagedObject#getIcon()} or
 * {@link ObjectSpecification#getIcon(org.apache.causeway.core.metamodel.object.ManagedObject)}.
 *
 * @apiNote internal service, used by the metamodel
 *
 * @see ManagedObject#getIcon()
 * @see ObjectSpecification#getIcon(org.apache.causeway.core.metamodel.object.ManagedObject)
 * @since 2.0
 */
public interface ObjectIconService {

    /**
     * {@link ObjectIcon} for given {@link ObjectSpecification}
     * and iconNameSuffix.
     * @return non-null
     */
    ObjectIcon getObjectIcon(
            @NonNull ObjectSpecification specification,
            @Nullable String iconNameSuffixIfAny);

}
