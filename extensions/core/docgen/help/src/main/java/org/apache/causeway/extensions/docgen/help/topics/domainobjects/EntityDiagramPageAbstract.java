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

import jakarta.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.services.metamodel.ObjectGraph;
import org.apache.causeway.extensions.docgen.help.CausewayModuleExtDocgenHelp;
import org.apache.causeway.extensions.docgen.help.applib.HelpPage;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;
import org.apache.causeway.valuetypes.asciidoc.builder.plantuml.obj.ObjectGraphRendererPlantuml;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@Named(CausewayModuleExtDocgenHelp.NAMESPACE + ".EntityDiagramPageAbstract")
@RequiredArgsConstructor
public abstract class EntityDiagramPageAbstract implements HelpPage {

    protected final MetaModelService metaModelService;

    @Override
    public AsciiDoc getContent() {
        return AsciiDoc.valueOf(renderObjectGraph(createObjectGraph()));
    }

    /** Governs which types to include with the diagram. */
    protected abstract boolean accept(final BeanSort beanSort, LogicalType logicalType);

    /**
     * Creates the diagram model. Can be overwritten to customize.
     */
    protected ObjectGraph createObjectGraph() {
        val objectGraph = metaModelService.exportObjectGraph(this::accept);
        return objectGraph;
    }

    /**
     * Returns ascii-doc syntax with Plantuml rendered diagrams. Can be overwritten to customize.
     */
    protected String renderObjectGraph(final ObjectGraph objectGraph) {
        val plantumlSource = objectGraph.render(new ObjectGraphRendererPlantuml());

        return  "== " + getTitle() + "\n\n"
            + _DiagramUtils.plantumlBlock(plantumlSource)
            + "\n"
            + _DiagramUtils.plantumlSourceBlock(plantumlSource);
    }

}

