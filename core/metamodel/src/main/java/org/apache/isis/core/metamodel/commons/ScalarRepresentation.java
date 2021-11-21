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
package org.apache.isis.core.metamodel.commons;

import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;

/**
 * Mode of representation for a Scalar within the UI.
 */
public enum ScalarRepresentation {

    /**
     * Editing (text-parsing) mode, corresponds to 'regular' UI components.
     * <p>
     * In case of value-types, indicates that for value-type to {@link String} conversion,
     * and vice versa, a {@link Parser} is required.
     */
    EDITING,

    /**
     * Viewing (HTML-rendering) mode, corresponds to 'compact' UI components.
     * <p>
     * In case of value-types, indicates that for value-type to {@link String} conversion,
     * a {@link Renderer} is required.
     */
    VIEWING;

    public boolean isEditing() {
        return this == EDITING;
    }

    public boolean isViewing() {
        return this == VIEWING;
    }
}
