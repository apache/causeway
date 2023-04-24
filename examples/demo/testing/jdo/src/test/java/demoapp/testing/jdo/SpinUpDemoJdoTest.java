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
package demoapp.testing.jdo;

import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.MmSpecUtils;
import org.apache.causeway.testing.unittestsupport.applib.util.ApprovalUtils;

import lombok.val;

@SpringBootTest(
        classes = {
                DemoDomainJdo_forTesting.class
        },
        properties = {
        })
@ActiveProfiles(profiles = "demo-jdo")
class SpinUpDemoJdoTest {

    @Autowired MetaModelContext mmc;

    @BeforeAll
    static void beforeAll() {
        // enables .yaml for approval testing's text compare
        ApprovalUtils.registerFileExtensionForTextCompare(".yaml");
    }

    @Test @Disabled("missing DomainObjectAliasedJdo, ...") //TODO demo domain is currently WIP
    @DisplayName("verifyAllSpecificationsDiscovered")
    @UseReporter(DiffReporter.class)
    void verify() {

        val specificationsBySortAsYaml =
                MmSpecUtils.specificationsBySortAsYaml(mmc.getSpecificationLoader());

        //debug
        //System.err.printf("%s%n", specificationsBySortAsYaml);

        // verify against approved run
        Approvals.verify(specificationsBySortAsYaml, new Options()
                .forFile()
                .withExtension(".yaml"));
    }

}