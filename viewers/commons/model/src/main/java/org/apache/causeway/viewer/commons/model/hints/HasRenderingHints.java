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
package org.apache.causeway.viewer.commons.model.hints;

import org.apache.causeway.core.metamodel.commons.ScalarRepresentation;

public interface HasRenderingHints {

    /**
     * @apiNote Similar to {@code #mustBeEditable()}, though not called from the same locations.
     * My suspicion is that it amounts to more or less the same set of conditions.
     */
    boolean isInlinePrompt();

    RenderingHint getRenderingHint();

    ScalarRepresentation getMode();
    @Deprecated// make immutable? - need to recreate any bound UI components anyway
    void setMode(ScalarRepresentation mode);

    // -- SHORTCUTS

    default boolean isViewMode() {
        return getMode() == ScalarRepresentation.VIEWING;
    }

    default boolean isEditMode() {
        return getMode() == ScalarRepresentation.EDITING;
    }

    default HasRenderingHints toEditMode() {
        setMode(ScalarRepresentation.EDITING);
        return this;
    }

    default HasRenderingHints toViewMode() {
        setMode(ScalarRepresentation.VIEWING);
        return this;
    }

}