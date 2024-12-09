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
package org.apache.causeway.viewer.commons.model.decorators;

import java.util.Optional;

import org.apache.causeway.applib.fa.FontAwesomeLayers;

/**
 * @param <T> UI component type to decorate
 * @param <R> resulting UI component type
 */
@FunctionalInterface
public interface IconDecorator<T, R> {

    /**
     * corresponds to the webjars path as provided by the maven artifact
     * {@code org.webjars:font-awesome}
     */
    public static final String FONTAWESOME_RESOURCE = "font-awesome/6.7.1/css/all.min.css";

    R decorate(T uiComponent, Optional<FontAwesomeLayers> faLayers);

}
