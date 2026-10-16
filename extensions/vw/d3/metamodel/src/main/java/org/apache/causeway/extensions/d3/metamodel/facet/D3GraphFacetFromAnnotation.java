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
package org.apache.causeway.extensions.d3.metamodel.facet;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import jakarta.inject.Inject;

import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetWithAttributes;
import org.apache.causeway.extensions.d3.applib.annotations.D3Graph;
import org.apache.causeway.extensions.d3.applib.config.D3Config;
import org.apache.causeway.extensions.d3.applib.spi.D3GraphAdvisor;

public class D3GraphFacetFromAnnotation extends D3GraphFacetAbstract implements FacetWithAttributes {

    private final int initialWidth;
    private final int initialHeight;
    private final boolean enableDragging;
    private final boolean enableZoom;
    private final int linkDistance;
    private final int repulsionStrength;
    private final int collisionRadius;
    private final double distributionStrength;
    private final String clickAction;

    @Inject List<D3GraphAdvisor> advisors;

    public D3GraphFacetFromAnnotation(final D3Config config, final FacetHolder holder) {
        super(config, holder);
        initialWidth = config.getInitialWidth();
        initialHeight = config.getInitialHeight();
        enableDragging = config.isEnableDragging();
        enableZoom = config.isEnableZoom();
        linkDistance = config.getLinkDistance();
        repulsionStrength = config.getRepulsionStrength();
        collisionRadius = config.getCollisionRadius();
        distributionStrength = config.getDistributionStrength();
        clickAction = config.getClickAction();
    }

    public static D3GraphFacetFromAnnotation create(
            final D3Graph annotation,
            final FacetHolder holder) {

        var config = new D3Config()
                .withInitialWidth(annotation.initialWidth())
                .withInitialHeight(annotation.initialHeight())
                .withEnableDragging(annotation.enableDragging())
                .withEnableZoom(annotation.enableZoom())
                .withLinkDistance(annotation.linkDistance())
                .withRepulsionStrength(annotation.repulsionStrength())
                .withCollisionRadius(annotation.collisionRadius())
                .withDistributionStrength(annotation.distributionStrength())
                .withClickAction(annotation.clickAction());

        return new D3GraphFacetFromAnnotation(config, holder);
    }

    @Override
    public D3Config configFor(final Class<?> domainClass) {
        var config = super.configFor(domainClass);

        return firstMatchingAdvice(domainClass)
                .map(advice -> {
                    var configFromAdvisor = config;
                    final Integer width = advice.initialWidth();
                    if(width != null) {
                        configFromAdvisor = configFromAdvisor.withInitialWidth(width);
                    }
                    final Integer height = advice.initialHeight();
                    if(height != null) {
                        configFromAdvisor = configFromAdvisor.withInitialHeight(height);
                    }
                    configFromAdvisor = configFromAdvisor
                            .withEnableDragging(advice.enableDragging())
                            .withEnableZoom(advice.enableZoom());
                    final String clickAction = advice.clickAction();
                    if(clickAction != null) {
                        configFromAdvisor = configFromAdvisor.withClickAction(clickAction);
                    }
                    return configFromAdvisor;
                })
                .orElse(config);
    }

    @Override
    public Optional<String> getClickAction(final Class<?> domainClass) {
        return Optional.ofNullable(configFor(domainClass).getClickAction())
                .filter(s -> !s.isBlank());
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("initialWidth", initialWidth);
        visitor.accept("initialHeight", initialHeight);
        visitor.accept("enableDragging", enableDragging);
        visitor.accept("enableZoom", enableZoom);
        visitor.accept("linkDistance", linkDistance);
        visitor.accept("repulsionStrength", repulsionStrength);
        visitor.accept("collisionRadius", collisionRadius);
        visitor.accept("distributionStrength", distributionStrength);
        visitor.accept("clickAction", clickAction);
    }

    // -- HELPER

    private Optional<D3GraphAdvisor.Advice> firstMatchingAdvice(final Class<?> domainClass) {
        if(advisors == null) return Optional.empty();
        return advisors.stream()
                .map(advisor -> advisor.advise(domainClass))
                .filter(advice -> advice != null)
                .findFirst();
    }

}

