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

package org.apache.isis.core.metamodel.facets.object.mixin;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.core.metamodel.facets.SingleValueFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Applies to {@link ObjectSpecification}s of classes that can act as a mix-in. 
 * <p>
 * Mix-ins are annotated e.g. {@link DomainObject} with
 * {@link DomainObject#nature()} of {@link Nature#MIXIN}) and have a public 1-arg 
 * constructor accepting an object (the mix-in's <i>holder</i>), being the object 
 * this is a mix-in for.
 * <p>
 * Since 2.0 there are additional annotations, that when declared on a type, make
 * the type recognized as a mix-in. These are {@link Action}, {@link Property} and
 * {@link Collection}.   
 */
public interface MixinFacet extends SingleValueFacet<String> {

    boolean isMixinFor(Class<?> candidateDomainType);

    enum Policy {
        FAIL_FAST,
        IGNORE_FAILURES
    }

    /**
     * Returns the (adapter of the) domain object that is the <i>holder</i> of the 
     * given mix-in adapter.
     */
    ManagedObject mixedIn(ManagedObject mixinAdapter, Policy policy);

    /**
     * Returns the mix-in around the provided domain object (<i>holder</i>)
     */
    Object instantiate(Object holderPojo);

    /**
     * Whether given method matches as a candidate for the mixin's main method. 
     * @param method
     * @return whether has expected name and same declaring class
     */
    boolean isCandidateForMain(Method method);


}
