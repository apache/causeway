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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

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
import org.apache.causeway.applib.services.metamodel.MetaModelServiceMenu.ExportFormat;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.base._Strings.KeyValuePair;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
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

        final Clob metaModelClob = factoryService
                .mixin(MetaModelServiceMenu.downloadMetaModel.class, metaModelServiceMenu)
                .act("metamodel", namespaces(), true, ExportFormat.XML, false)
                .toClob(StandardCharsets.UTF_8);
        final String xml = metaModelClob
                .asString();

        Approvals.verify(xml, options());
    }

    // -- HELPER

    private Options options() {
        return new Options()
                .withScrubber(this::scrub)
                .forFile()
                .withExtension(".xml");
    }

    private List<String> namespaces() {
        return List.of("org.apache.causeway.testdomain.model.good");
    }

    private String scrub(final String input) {
        return _Text.streamLines(input)
                .map(this::scrubLine)
                .filter(line->!_Strings.nullToEmpty(line).isBlank()) // ignore blank lines, just in case
                .collect(Collectors.joining("\n")); // UNIX line ending convention
    }

    /**
     * As the XML spec states, order of attributes has no semantic significance and hence is not
     * guaranteed to be always the same, like in
     * <pre>
     * {@code <mml:param xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="mml:scalarParam" id="style">}
     * </pre>
     * So we have to scrub those for consistent comparison.
     * @param line
     * @return canonical form of the line
     */
    private String scrubLine(final String line) {

        val magicPrefix = "<mml:param ";
        val magicSuffix = ">";
        int p = line.indexOf(magicPrefix);
        if(p<0) {
            return line;
        }
        p += magicPrefix.length(); // pointer at end of "...<mml:param "
        int q = line.lastIndexOf(magicSuffix); // pointer at start of "... >"

        val chunks = _Lists.<String>newArrayList();
        chunks.add(line.substring(0, p-1)); // first chunk "...<mml:param"

        // ordered attributes
        val attrs = _Maps.<String, KeyValuePair>newTreeMap();
        _Strings.splitThenStream(line.substring(p, q), " ")
        .map(attrLiteral->
            _Strings.parseKeyValuePair(attrLiteral, '=')
                    .orElseGet(()->_Strings.pair(attrLiteral, null))
        )
        .forEach(attr->attrs.put(attr.getKey(), attr));

        // collect all chunks
        attrs.values()
        .forEach(attr->chunks.add(
            attr.getValue()!=null
                ? " " + attr.getKey() + "=" + attr.getValue()
                : " " + attr.getKey()));
        chunks.add(magicSuffix);

        // reassemble line
        return chunks.stream().collect(Collectors.joining());
    }

}
