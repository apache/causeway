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

package org.apache.isis.metamodel.facets.object.mixin;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.SingleValueFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;

/**
 * Applies to {@link ObjectSpecification}s of classes that can act as a mix-in, namely that they are annotated
 * appropriately (eg {@link org.apache.isis.applib.annotation.Mixin} or {@link DomainObject} with
 * {@link DomainObject#nature()} of {@link Nature#MIXIN}) and which have a 1-arg constructor accepting an object
 * (being the object this is a mix-in for).
 */
public interface MixinFacet extends SingleValueFacet<String> {

    boolean isMixinFor(Class<?> candidateDomainType);

    enum Policy {
        FAIL_FAST,
        IGNORE_FAILURES
    }

    /**
     * Returns the (adapter of the) domain object that a mixin adapter contains.
     */
    ObjectAdapter mixedIn(ObjectAdapter mixinAdapter, final Policy policy);

    /**
     * Returns the mixin around the provided domain object
     */
    Object instantiate(Object domainPojo);



}
