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
package org.apache.isis.core.metamodel.facets.object.value;

import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;

/**
 * Introduced specifically for value-types.
 *
 * @apiNote there are similarities with org.apache.isis.viewer.common.model.object.ObjectUiModel.EitherViewOrEdit,
 * which is not specific to value-types, but covers any scalars;
 * (thinking about unifying these two into one - more generic - enum)
 */
public enum ValueRepresentation {

    /** Indicates that for value-type to {@link String} conversion a {@link Parser} is required. */
    EDITING,

    /** Indicates that for value-type to {@link String} conversion a {@link Renderer} is required. */
    RENDERING;

    public boolean isEditing() {
        return this == EDITING;
    }

    public boolean isRendering() {
        return this == RENDERING;
    }
}
