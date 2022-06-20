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
package org.apache.isis.testdomain.domainmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Inject;

import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.metamodel.MetaModelServiceMenu;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.good.Configuration_usingValidDomain;

import lombok.SneakyThrows;
import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingValidDomain.class,

        },
        properties = {
                "isis.core.meta-model.introspector.mode=FULL",
                "isis.applib.annotation.domain-object.editing=TRUE",
                "isis.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
          })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
//uncomment if intended only for manual verification.
//@DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
class MetaModelRegressionTest {

    @Inject MetaModelServiceMenu metaModelServiceMenu;
    @Inject FactoryService factoryService;

    @BeforeEach
    void setUp() {
        assertNotNull(metaModelServiceMenu);

        val url = _Resources.getResourceUrl(getClass(), "/metamodel.xml.zip");
        if(url==null) {
            //createReferenceMetaModelFile(new File("/<path to resources>/metamodel.xml.zip"));
            _Exceptions.throwNotImplemented();
        }

        //for maintenance
        //createReferenceMetaModelFile(new File("d:/tmp/_scratch/metamodel.xml.zip"));
    }

    @Test
    @SneakyThrows
    void metaModelDiff_compareWithLastKnownGood() {

        Assumptions.assumeThat(getClass().getName()).contains("isis");  // disable if rename, as the .zip file needs to be updated.

        val downloadMetaModelDiff =
                factoryService.mixin(MetaModelServiceMenu.downloadMetaModelDiff.class, metaModelServiceMenu);
        val metamodelExport =
                downloadMetaModelDiff.act("metamodel.xml", namespaces(), true,
                        referenceMetaModelAsZippedBlob())
                .unZip(CommonMimeType.XML)
                .toClob(StandardCharsets.UTF_8);

        val sw = new StringWriter();
        metamodelExport.writeCharsTo(sw);

        val diff = sw.toString();

        if(!diff.isBlank()) {
            System.err.printf("%s%n", diff);
            fail("Reference meta-model and current do differ.");
        }
    }

    // -- HELPER

    @SneakyThrows
    private void createReferenceMetaModelFile(final File file) {
        try(val fos = new FileOutputStream(file)){
            currentMetaModelAsZippedBlob().writeBytesTo(fos);
        }
    }

    @SneakyThrows
    private Blob referenceMetaModelAsZippedBlob() {
        val bytes = _Bytes.of(_Resources.load(getClass(), "/metamodel.xml.zip"));
        return Blob.of("metamodel.xml", CommonMimeType.ZIP, bytes);
    }

    private Blob currentMetaModelAsZippedBlob() {
        val downloadMetaModelXml =
                factoryService.mixin(MetaModelServiceMenu.downloadMetaModelXml.class, metaModelServiceMenu);
        return downloadMetaModelXml.act("metamodel.xml", namespaces(), true);
    }

    private List<String> namespaces() {
        return List.of("org.apache.isis.testdomain.model.good");
    }

}
