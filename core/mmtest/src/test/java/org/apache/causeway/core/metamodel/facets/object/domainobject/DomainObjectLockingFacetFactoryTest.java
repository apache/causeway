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
package org.apache.causeway.core.metamodel.facets.object.domainobject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Locking;
import org.apache.causeway.core.config.metamodel.facets.DomainObjectConfigOptions.LockingPolicy;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.domainobject.locking.LockingFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.locking.LockingFacetForDomainObjectAnnotationAsConfigured;
import org.apache.causeway.core.metamodel.facets.object.domainobject.locking.LockingFacetFromConfiguration;
import org.apache.causeway.core.metamodel.facets.object.locking.LockingFacet;

class DomainObjectLockingFacetFactoryTest extends FacetFactoryTestAbstract {

    static class Unannotated { }
    @DomainObject static class Default { }
    @DomainObject(locking = Locking.PESSIMISTIC) static class Pessimistic { }
    @DomainObject(locking = Locking.OPTIMISTIC) static class Optimistic { }
    @DomainObject static class Inherited extends Pessimistic { }
    @DomainObject(locking = Locking.AS_CONFIGURED) static class Configured extends Pessimistic { }

    @Retention(RetentionPolicy.RUNTIME)
    @DomainObject(locking = Locking.PESSIMISTIC)
    @interface Locked { }
    @Locked @DomainObject static class MetaAnnotated { }

    @Test
    void defaultsToOptimistic() {
        check(Default.class, LockingPolicy.OPTIMISTIC, LockingFacetForDomainObjectAnnotationAsConfigured.class);
        check(Unannotated.class, LockingPolicy.OPTIMISTIC, LockingFacetFromConfiguration.class);
    }

    @Test
    void explicitPoliciesOverrideConfiguration() {
        check(Pessimistic.class, LockingPolicy.PESSIMISTIC, LockingFacetForDomainObjectAnnotation.class);
        configure(LockingPolicy.PESSIMISTIC);
        check(Optimistic.class, LockingPolicy.OPTIMISTIC, LockingFacetForDomainObjectAnnotation.class);
    }

    @Test
    void defaultAndUnannotatedUseConfiguration() {
        configure(LockingPolicy.PESSIMISTIC);
        check(Default.class, LockingPolicy.PESSIMISTIC, LockingFacetForDomainObjectAnnotationAsConfigured.class);
        check(Unannotated.class, LockingPolicy.PESSIMISTIC, LockingFacetFromConfiguration.class);
    }

    @Test
    void defaultAllowsInheritedAndMetaAnnotationPolicies() {
        check(Inherited.class, LockingPolicy.PESSIMISTIC, LockingFacetForDomainObjectAnnotation.class);
        check(MetaAnnotated.class, LockingPolicy.PESSIMISTIC, LockingFacetForDomainObjectAnnotation.class);
    }

    @Test
    void asConfiguredOverridesInheritance() {
        check(Configured.class, LockingPolicy.OPTIMISTIC, LockingFacetForDomainObjectAnnotationAsConfigured.class);
    }

    private void configure(final LockingPolicy policy) {
        setup(builder -> builder.testPropertyValues(TestPropertyValues.of(
                "causeway.applib.annotation.domain-object.locking=" + policy.name())));
    }

    private void check(final Class<?> type, final LockingPolicy policy, final Class<?> implementation) {
        final DomainObjectAnnotationFacetFactory factory = new DomainObjectAnnotationFacetFactory(getMetaModelContext());
        objectScenario(type, (context, holder) -> {
            factory.processLocking(context.synthesizeOnType(DomainObject.class), context);
            final LockingFacet facet = holder.lookupFacet(LockingFacet.class).orElseThrow();
            assertEquals(policy, facet.getPolicy());
            assertInstanceOf(implementation, facet);
            assertNoMethodsRemoved();
        });
    }
}
