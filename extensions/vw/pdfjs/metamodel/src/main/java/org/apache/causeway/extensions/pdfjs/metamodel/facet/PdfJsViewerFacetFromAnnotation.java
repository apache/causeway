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
package org.apache.causeway.extensions.pdfjs.metamodel.facet;

import java.util.List;
import java.util.function.BiConsumer;

import javax.inject.Inject;

import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetWithAttributes;
import org.apache.causeway.extensions.pdfjs.applib.annotations.PdfJsViewer;
import org.apache.causeway.extensions.pdfjs.applib.config.PdfJsConfig;
import org.apache.causeway.extensions.pdfjs.applib.config.Scale;
import org.apache.causeway.extensions.pdfjs.applib.spi.PdfJsViewerAdvisor;

public class PdfJsViewerFacetFromAnnotation extends PdfJsViewerFacetAbstract implements FacetWithAttributes {

    private final int initialHeight;
    private final int initialPage;
    private final Scale initialScale;

    @Inject List<PdfJsViewerAdvisor> advisors;

    public PdfJsViewerFacetFromAnnotation(final PdfJsConfig config, final FacetHolder holder) {
        super(config, holder);
        initialHeight = config.getInitialHeight();
        initialPage = config.getInitialPage();
        initialScale = config.getInitialScale();
    }

    public static PdfJsViewerFacetFromAnnotation create(
            final PdfJsViewer annotation,
            final FacetHolder holder) {

        var config = new PdfJsConfig()
                .withInitialPage(annotation.initialPageNum())
                .withInitialScale(annotation.initialScale())
                .withInitialHeight(annotation.initialHeight());

        return new PdfJsViewerFacetFromAnnotation(config, holder);
    }

    @Override
    public PdfJsConfig configFor(final PdfJsViewerAdvisor.InstanceKey instanceKey) {
        var config = super.configFor(instanceKey);

        if(advisors != null) {
            for (PdfJsViewerAdvisor advisor : advisors) {
                final PdfJsViewerAdvisor.Advice advice = advisor.advise(instanceKey);
                if(advice != null) {
                    final Integer pageNum = advice.getPageNum();
                    if(pageNum != null) {
                        config = config.withInitialPage(pageNum);
                    }
                    final Scale scale = advice.getScale();
                    if(scale != null) {
                        config = config.withInitialScale(scale);
                    }
                    final Integer height = advice.getHeight();
                    if(height != null) {
                        config = config.withInitialHeight(height);
                    }
                    break;
                }
            }
        }

        return config;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("initialScale", initialScale);
        visitor.accept("initialHeight", initialHeight);
        visitor.accept("initialPage", initialPage);
    }

}
