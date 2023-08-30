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
package org.apache.causeway.extensions.docgen.help.topics.domainobjects;

import java.util.Optional;
import java.util.stream.Stream;

import jakarta.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.docgen.help.CausewayModuleExtDocgenHelp;
import org.apache.causeway.extensions.docgen.help.applib.HelpPage;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@Named(CausewayModuleExtDocgenHelp.NAMESPACE + ".EntityDiagramPageAbstract")
@RequiredArgsConstructor
public abstract class EntityDiagramPageAbstract implements HelpPage {

    private final SpecificationLoader specLoader;
    private final CausewayBeanTypeRegistry beanTypeRegistry;

    @Override
    public AsciiDoc getContent() {
        val title = getTitle();
        val plantumlSource = entityTypesAsDiagram();

        return AsciiDoc.valueOf(
                "== " + title + "\n\n"
                + _DiagramUtils.plantumlBlock(plantumlSource)
                + "\n"
                + _DiagramUtils.plantumlSourceBlock(plantumlSource));
    }

    /** governs which entities to include */
    protected abstract boolean accept(final ObjectSpecification objSpec);

    // -- HELPER

    private String entityTypesAsDiagram() {
        val objectGraph = new ObjectGraph();
        streamEntityTypes()
            .filter(this::accept)
            .forEach(objSpec->objectGraph.registerObject(objSpec));
        return objectGraph.render();
    }

    private Stream<ObjectSpecification> streamEntityTypes() {
        return beanTypeRegistry.getEntityTypes().keySet()
            .stream()
            .map(specLoader::specForType)
            .filter(Optional::isPresent)
            .map(Optional::get);
    }

}

