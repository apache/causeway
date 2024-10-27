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

import javax.inject.Named;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.services.metamodel.objgraph.ObjectGraph;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.extensions.docgen.help.CausewayModuleExtDocgenHelp;
import org.apache.causeway.extensions.docgen.help.applib.HelpPage;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;
import org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocBuilder;
import org.apache.causeway.valuetypes.asciidoc.builder.AsciiDocFactory;
import org.apache.causeway.valuetypes.asciidoc.builder.objgraph.d3js.ObjectGraphRendererD3js;
import org.apache.causeway.valuetypes.asciidoc.builder.objgraph.d3js.ObjectGraphRendererD3js.GraphRenderOptions;
import org.apache.causeway.valuetypes.asciidoc.builder.objgraph.d3js.ObjectGraphRendererEdgeListing;
import org.apache.causeway.valuetypes.asciidoc.builder.objgraph.plantuml.ObjectGraphRendererPlantuml;

import lombok.RequiredArgsConstructor;

@Component
@Named(CausewayModuleExtDocgenHelp.NAMESPACE + ".EntityDiagramPageAbstract")
@RequiredArgsConstructor
public abstract class EntityDiagramPageAbstract implements HelpPage {

    protected final MetaModelService metaModelService;

    @Override
    public AsciiDoc getContent() {
        return new AsciiDocBuilder()
                .append(doc->{
                    var mainBlock = AsciiDocFactory.block(doc);
                    _Strings.nonEmpty(diagramTitle())
                        .ifPresent(mainBlock::setTitle);
                    mainBlock.setSource(renderObjectGraph(createObjectGraph()));
                })
                .buildAsValue();
    }

    /** Governs which types to include with the diagram. */
    protected abstract boolean accept(final BeanSort beanSort, LogicalType logicalType);

    /**
     * Renders given {@link ObjectGraph} and returns ascii-doc syntax.
     */
    protected abstract String renderObjectGraph(final ObjectGraph objectGraph);

    /**
     * Title of the diagram (not page), if any.
     */
    protected abstract @Nullable String diagramTitle();

    /**
     * Creates the diagram model. Can be overwritten to customize.
     */
    protected ObjectGraph createObjectGraph() {
        var objectGraph = metaModelService.exportObjectGraph(this::accept)
                .transform(ObjectGraph.Transformers.relationMerger());
        return objectGraph;
    }

    /**
     * Can be used by sub class when implementing {@link #renderObjectGraph(ObjectGraph)}.
     */
    protected String renderObjectGraphUsingD3js(final ObjectGraph objectGraph) {
        var d3jsSourceAsHtml = objectGraph.render(
                new ObjectGraphRendererD3js(GraphRenderOptions.builder().build()));
        return new AsciiDocBuilder()
                .append(doc->AsciiDocFactory.htmlPassthroughBlock(doc, d3jsSourceAsHtml))
                .append(doc->{
                    var source = objectGraph.render(new ObjectGraphRendererEdgeListing());
                    var sourceBlock = AsciiDocFactory.sourceBlock(doc, "txt", source);
                    sourceBlock.setTitle("Edge Listing");
                })
                .buildAsString();
    }

    /**
     * Can be used by sub class when implementing {@link #renderObjectGraph(ObjectGraph)}.
     */
    protected String renderObjectGraphUsingPlantuml(final ObjectGraph objectGraph) {
        var plantumlSource = objectGraph.render(new ObjectGraphRendererPlantuml());
        return _DiagramUtils.plantumlBlock(plantumlSource)
            + "\n"
            + _DiagramUtils.plantumlSourceBlock(plantumlSource);
    }

}

