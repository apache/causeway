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

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.extensions.secman.applib.mmm.MmmModule;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancyRepository;
import org.apache.causeway.extensions.secman.jpa.CausewayModuleExtSecmanPersistenceJpa;
import org.apache.causeway.persistence.jpa.applib.services.JpaSupportService;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = ApplicationTenancy_IntegTest.AppManifest.class
)
@ActiveProfiles("test")
@Transactional
class ApplicationTenancy_IntegTest extends CausewayIntegrationTestAbstract {

    @Test
    void tenancyParentAssignmentAndClearingAreConsistentAfterReload() {
        val parent = newTenancy("association-parent", "/association-parent", null);
        val child = newTenancy("association-child", "/association-child", null);

        tenancyRepository.setParentOnTenancy(child, parent);

        Assertions.assertThat(child.getParent()).isEqualTo(parent);
        Assertions.assertThat(parent.getChildren()).contains(child);

        flushAndClear();

        val reloadedParent = tenancy("/association-parent");
        val reloadedChild = tenancy("/association-child");
        Assertions.assertThat(reloadedChild.getParent()).isEqualTo(reloadedParent);
        Assertions.assertThat(reloadedParent.getChildren()).contains(reloadedChild);

        tenancyRepository.clearParentOnTenancy(reloadedChild);

        Assertions.assertThat(reloadedChild.getParent()).isNull();
        Assertions.assertThat(reloadedParent.getChildren()).doesNotContain(reloadedChild);

        flushAndClear();

        Assertions.assertThat(tenancy("/association-child").getParent()).isNull();
        Assertions.assertThat(tenancy("/association-parent").getChildren())
                .doesNotContain(tenancy("/association-child"));
    }

    @Test
    void tenancyReassignmentAndRepeatedOperationsRemainConsistent() {
        val firstParent = newTenancy("first-parent", "/first-parent", null);
        val secondParent = newTenancy("second-parent", "/second-parent", null);
        val child = newTenancy("reassigned-child", "/reassigned-child", firstParent);

        tenancyRepository.setParentOnTenancy(child, secondParent);
        tenancyRepository.setParentOnTenancy(child, secondParent);

        Assertions.assertThat(child.getParent()).isEqualTo(secondParent);
        Assertions.assertThat(firstParent.getChildren()).doesNotContain(child);
        Assertions.assertThat(secondParent.getChildren()).containsOnlyOnce(child);

        flushAndClear();

        val reloadedFirstParent = tenancy("/first-parent");
        val reloadedSecondParent = tenancy("/second-parent");
        val reloadedChild = tenancy("/reassigned-child");
        Assertions.assertThat(reloadedChild.getParent()).isEqualTo(reloadedSecondParent);
        Assertions.assertThat(reloadedFirstParent.getChildren()).doesNotContain(reloadedChild);
        Assertions.assertThat(reloadedSecondParent.getChildren()).containsOnlyOnce(reloadedChild);

        tenancyRepository.clearParentOnTenancy(reloadedChild);
        tenancyRepository.clearParentOnTenancy(reloadedChild);

        Assertions.assertThat(reloadedChild.getParent()).isNull();
        Assertions.assertThat(reloadedSecondParent.getChildren()).doesNotContain(reloadedChild);
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
        val entityManager = jpaSupport.getEntityManagerElseFail(
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
    @PropertySources({
            @PropertySource(CausewayPresets.UseLog4j2Test),
    })
    static class AppManifest {
    }
}
