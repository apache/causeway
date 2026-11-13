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
package org.apache.causeway.extensions.secman.jpa.tenancy.dom;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.extensions.secman.applib.mmm.MmmModule;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancyRepository;
import org.apache.causeway.extensions.secman.jpa.CausewayModuleExtSecmanPersistenceJpa;
import org.apache.causeway.persistence.jpa.applib.services.JpaSupportService;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

@SpringBootTest(
        classes = ApplicationTenancy_IntegTest.AppManifest.class
)
@ActiveProfiles("test")
@Transactional
class ApplicationTenancy_IntegTest extends CausewayIntegrationTestAbstract {

    @Test
    void tenancyParentAssignmentAndClearingAreConsistentAfterReload() {
        var parent = newTenancy("association-parent", "/association-parent", null);
        var child = newTenancy("association-child", "/association-child", null);

        tenancyRepository.setParentOnTenancy(child, parent);

        assertThat(child.getParent()).isEqualTo(parent);
        assertThat(parent.getChildren()).contains(child);

        flushAndClear();

        var reloadedParent = tenancy("/association-parent");
        var reloadedChild = tenancy("/association-child");
        assertThat(reloadedChild.getParent()).isEqualTo(reloadedParent);
        assertThat(reloadedParent.getChildren()).contains(reloadedChild);

        tenancyRepository.clearParentOnTenancy(reloadedChild);

        assertThat(reloadedChild.getParent()).isNull();
        assertThat(reloadedParent.getChildren()).doesNotContain(reloadedChild);

        flushAndClear();

        assertThat(tenancy("/association-child").getParent()).isNull();
        assertThat(tenancy("/association-parent").getChildren())
                .doesNotContain(tenancy("/association-child"));
    }

    @Test
    void tenancyReassignmentAndRepeatedOperationsRemainConsistent() {
        var firstParent = newTenancy("first-parent", "/first-parent", null);
        var secondParent = newTenancy("second-parent", "/second-parent", null);
        var child = newTenancy("reassigned-child", "/reassigned-child", firstParent);

        tenancyRepository.setParentOnTenancy(child, secondParent);
        tenancyRepository.setParentOnTenancy(child, secondParent);

        assertThat(child.getParent()).isEqualTo(secondParent);
        assertThat(firstParent.getChildren()).doesNotContain(child);
        assertThat(secondParent.getChildren()).containsOnlyOnce(child);

        flushAndClear();

        var reloadedFirstParent = tenancy("/first-parent");
        var reloadedSecondParent = tenancy("/second-parent");
        var reloadedChild = tenancy("/reassigned-child");
        assertThat(reloadedChild.getParent()).isEqualTo(reloadedSecondParent);
        assertThat(reloadedFirstParent.getChildren()).doesNotContain(reloadedChild);
        assertThat(reloadedSecondParent.getChildren()).containsOnlyOnce(reloadedChild);

        tenancyRepository.clearParentOnTenancy(reloadedChild);
        tenancyRepository.clearParentOnTenancy(reloadedChild);

        assertThat(reloadedChild.getParent()).isNull();
        assertThat(reloadedSecondParent.getChildren()).doesNotContain(reloadedChild);
    }

    private ApplicationTenancy newTenancy(
            final String name,
            final String path,
            final ApplicationTenancy parent) {
        return tenancyRepository.newTenancy(name, path, parent);
    }

    private ApplicationTenancy tenancy(final String path) {
        return tenancyRepository.findByPath(path);
    }

    private void flushAndClear() {
        var entityManager = jpaSupport.getEntityManagerElseFail(
                org.apache.causeway.extensions.secman.jpa.tenancy.dom.ApplicationTenancy.class);
        entityManager.flush();
        entityManager.clear();
    }

    @Inject private JpaSupportService jpaSupport;
    @Inject private ApplicationTenancyRepository tenancyRepository;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            CausewayModuleCoreRuntimeServices.class,
            CausewayModuleSecurityBypass.class,
            CausewayModuleExtSecmanPersistenceJpa.class,
            MmmModule.class,
    })
    public static class AppManifest {
    }
}
