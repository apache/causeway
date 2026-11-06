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
package org.apache.causeway.extensions.d3.applib.config;

import java.io.Serial;
import java.io.Serializable;

import org.springframework.util.Assert;

import org.apache.causeway.commons.io.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

/**
 * Configuration holder for the D3 viewer extension.
 * <p>
 * Allows programmatic or configuration-based override of default D3 graph settings.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Getter @AllArgsConstructor @NoArgsConstructor @Builder
public class D3Config implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private int initialWidth = 800;

    @Builder.Default
    private int initialHeight = 600;

    @Builder.Default
    private boolean enableDragging = true;

    @Builder.Default
    private boolean enableZoom = true;

    /**
     * The distance (pixels) between linked nodes in the force layout.
     * Higher values spread connected nodes further apart. (default: 120)
     */
    @Builder.Default
    private int linkDistance = 120;

    /**
     * The strength of the repulsive force between all nodes (negative = repulsion).
     * More negative values push nodes further apart. (default: -400)
     */
    @Builder.Default
    private int repulsionStrength = -400;

    /**
     * The minimum radial separation (pixels) maintained between any two nodes
     * via collision detection. Prevents overlap and clusters. (default: 60)
     */
    @Builder.Default
    private int collisionRadius = 60;

    /**
     * The strength of the centering distribution force that pulls nodes toward
     * the center of the canvas. Lower values let nodes spread further out.
     * Range: 0.0 (no centering) to 1.0 (tight cluster). (default: 0.08)
     */
    @Builder.Default
    private double distributionStrength = 0.08;

    /**
     * The member id of a domain action to invoke (AJAX) when a graph node is clicked;
     * empty (the default) means no action is bound.
     */
    @Builder.Default
    private String clickAction = "";

    /**
     * The html element id of the D3 graph canvas (set by the viewer, not by the domain model).
     */
    @With
    private CharSequence canvasId;

    /**
     * The graph data as a JSON string (the D3 graph model), set by the viewer
     * from the annotated property's value or from a {@code D3GraphAdvisor}.
     */
    @With
    private CharSequence graphData;

    /**
     * The AJAX callback URL to invoke when a graph node is clicked; if {@code null},
     * clicking a node has no effect. Set by the viewer.
     */
    @With
    private CharSequence clickCallbackUrl;

    public D3Config withInitialWidth(final int initialWidth) {
        Assert.isTrue(initialWidth >= 200 && initialWidth <= 4000,
                ()->String.format("'initialWidth' must be between 200 and 4000; got %d", initialWidth));
        return asBuilder().initialWidth(initialWidth).build();
    }

    public D3Config withInitialHeight(final int initialHeight) {
        Assert.isTrue(initialHeight >= 200 && initialHeight <= 4000,
                ()->String.format("'initialHeight' must be between 200 and 4000; got %d", initialHeight));
        return asBuilder().initialHeight(initialHeight).build();
    }

    public D3Config withEnableDragging(final boolean enableDragging) {
        return asBuilder().enableDragging(enableDragging).build();
    }

    public D3Config withEnableZoom(final boolean enableZoom) {
        return asBuilder().enableZoom(enableZoom).build();
    }

    public D3Config withLinkDistance(final int linkDistance) {
        Assert.isTrue(linkDistance >= 20 && linkDistance <= 1000,
                ()->String.format("'linkDistance' must be between 20 and 1000; got %d", linkDistance));
        return asBuilder().linkDistance(linkDistance).build();
    }

    public D3Config withRepulsionStrength(final int repulsionStrength) {
        Assert.isTrue(repulsionStrength >= -5000 && repulsionStrength <= 0,
                ()->String.format("'repulsionStrength' must be between -5000 and 0; got %d", repulsionStrength));
        return asBuilder().repulsionStrength(repulsionStrength).build();
    }

    public D3Config withCollisionRadius(final int collisionRadius) {
        Assert.isTrue(collisionRadius >= 0 && collisionRadius <= 500,
                ()->String.format("'collisionRadius' must be between 0 and 500; got %d", collisionRadius));
        return asBuilder().collisionRadius(collisionRadius).build();
    }

    public D3Config withDistributionStrength(final double distributionStrength) {
        Assert.isTrue(distributionStrength >= 0.0 && distributionStrength <= 1.0,
                ()->String.format("'distributionStrength' must be between 0.0 and 1.0; got %f", distributionStrength));
        return asBuilder().distributionStrength(distributionStrength).build();
    }

    public D3Config withClickAction(final String clickAction) {
        return asBuilder().clickAction(clickAction).build();
    }

    public D3Config.D3ConfigBuilder asBuilder() {
        return D3Config.builder()
                .initialWidth(getInitialWidth())
                .initialHeight(getInitialHeight())
                .enableDragging(isEnableDragging())
                .enableZoom(isEnableZoom())
                .linkDistance(getLinkDistance())
                .repulsionStrength(getRepulsionStrength())
                .collisionRadius(getCollisionRadius())
                .distributionStrength(getDistributionStrength())
                .clickAction(getClickAction())
                .canvasId(getCanvasId())
                .graphData(getGraphData())
                .clickCallbackUrl(getClickCallbackUrl());
    }

    public String toJsonString() {
        return JsonUtils.toStringUtf8(this);
    }

}