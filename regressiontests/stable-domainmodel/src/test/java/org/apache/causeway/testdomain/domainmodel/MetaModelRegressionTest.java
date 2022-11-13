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
package org.apache.causeway.testdomain.domainmodel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.inject.Inject;

import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.metamodel.MetaModelServiceMenu;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.good.Configuration_usingValidDomain;

import lombok.SneakyThrows;
import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingValidDomain.class,

        },
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL",
                "causeway.applib.annotation.domain-object.editing=TRUE",
                "causeway.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
          })
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
//uncomment if intended only for manual verification.
//@DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
class MetaModelRegressionTest {

    @Inject MetaModelServiceMenu metaModelServiceMenu;
    @Inject FactoryService factoryService;

    @BeforeEach
    void setUp() {
        assertNotNull(metaModelServiceMenu);
    }

    @Test
    @SneakyThrows
    @UseReporter(DiffReporter.class)
    void verify() {

        // disable if rename, as the .zip file needs to be updated.
        // Assumptions.assumeThat(getClass().getName()).contains("causeway");

        Blob metaModelZip = factoryService.mixin(MetaModelServiceMenu.downloadMetaModelXml.class,
                metaModelServiceMenu).act("metamodel.xml", namespaces(), true);
        val xml = asXml(metaModelZip);

        Approvals.verify(xml, options());

    }

    private static String asXml(final Blob zip) throws IOException {
        val clob = zip.unZip(CommonMimeType.XML).toClob(StandardCharsets.UTF_8);
        return clob.asString();
    }

    private Options options() {
        return new Options().withScrubber(s -> s).forFile().withExtension(".xml");

    }

    // -- HELPER


    private List<String> namespaces() {
        return List.of("org.apache.causeway.testdomain.model.good");
    }

}
