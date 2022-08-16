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
package org.apache.isis.tooling.metaprog.demoshowcases.value;

import java.io.File;
import java.util.function.Consumer;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

public class ValueTypeGenTemplate {

    @Value @Builder
    public static class Config {
        final File outputRootDir;
        final String showcaseName;
    }

    @RequiredArgsConstructor
    enum Generator {
        DOC(".adoc"),
        JAVA(".java"),
        LAYOUT(".layout.xml");
        final String fileSuffix;
    }

    @RequiredArgsConstructor
    enum Sources {
        HOLDER("holder/%sHolder", Generator.JAVA),
        HOLDER2("holder/%sHolder2", Generator.JAVA),
        HOLDER_ACTION_RETURNING("holder/%sHolder_actionReturning", Generator.JAVA),
        HOLDER_ACTION_RETURNING_COLLECTION("holder/%sHolder_actionReturningCollection", Generator.JAVA),
        HOLDER_MIXIN_PROPERTY("holder/%sHolder_mixinProperty", Generator.JAVA),
        HOLDER_UPDATE_READONLY_OPTIONAL_PROPERTY("holder/%sHolder_updateReadOnlyOptionalProperty", Generator.JAVA),
        HOLDER_updateReadOnlyProperty("holder/%sHolder_updateReadOnlyProperty", Generator.JAVA),
        HOLDER_updateReadOnlyPropertyWithChoices("holder/%sHolder_updateReadOnlyPropertyWithChoices", Generator.JAVA),
        COLLECTION("%ss", Generator.JAVA),
        JDO("jdo/%sJdo", Generator.JAVA),
        JDO_ENTITIES("jdo/%sJdoEntities", Generator.JAVA),
        JPA("jpa/%sJpa", Generator.JAVA),
        JPA_ENTITIES("jpa/%sJpaEntities", Generator.JAVA),
        ENTITY("persistence/%sEntity", Generator.JAVA),
        SEEDING("persistence/%sSeeding", Generator.JAVA),
        SAMPLES("samples/%sSamples", Generator.JAVA),
        VIEWMODEL("vm/%sVm", Generator.JAVA),

        COMMON_DOC("%ss-common", Generator.DOC),
        DESCRIPTION("%ss-description", Generator.DOC),
        JDO_DESCRIPTION("jdo/%sJdo-description", Generator.DOC),
        JPA_DESCRIPTION("jpa/%sJpa-description", Generator.DOC),
        VIEWMODEL_DESCRIPTION("vm/%sVm-description", Generator.DOC),

        COLLECTION_LAYOUT("%ss", Generator.LAYOUT),
        ENTITY_LAYOUT("persistence/%sEntity", Generator.LAYOUT),
        VIEWMODEL_LAYOUT("vm/%sVm", Generator.LAYOUT)

        ;
        private final String pathTemplate;
        private final Generator generator;
        private final File file(final Config config) {
            return new File(config.getOutputRootDir(),
                    String.format(pathTemplate, config.getShowcaseName())
                    + generator.fileSuffix);
        }
    }

    public void generate(final Config config, final Consumer<File> onSourceGenerated) {

        for(var source: Sources.values()) {
            val gen = source.file(config);
            onSourceGenerated.accept(gen);
        }

    }

}
