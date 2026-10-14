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
package org.apache.causeway.extensions.d3.applib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a domain object <b>property</b> (or a domain <b>object</b>) to indicate that its
 * (or one of its properties) data should be rendered as an interactive D3.js graph in the
 * Wicket viewer.
 * <p>
 * When placed on a property, the property's value (expected to be a String in JSON or
 * a custom graph-representation type) is passed to the D3 viewer panel.
 * </p>
 * <p>
 * When placed on a domain object, the annotation can be used to indicate that the object's
 * default representation should use the D3 viewer. An SPI advisor can then resolve the
 * graph data per object instance.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
public @interface D3Graph {

    /**
     * The initial width of the SVG canvas in pixels.
     */
    int initialWidth() default 800;

    /**
     * The initial height of the SVG canvas in pixels.
     */
    int initialHeight() default 600;

    /**
     * Whether to enable node dragging.
     */
    boolean enableDragging() default true;

    /**
     * Whether to enable zoom and pan.
     */
    boolean enableZoom() default true;

    /**
     * The member id of a domain action to invoke (AJAX) when a node of the graph is clicked.
     * <p>
     * If specified, the action must accept a single string parameter
     * (the clicked node's id). If empty (the default), clicking a node has no effect.
     * </p>
     */
    String clickAction() default "";
}