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
package org.apache.isis.viewer.common.model.object;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public interface ObjectUiModel {

    ManagedObject getManagedObject();

    public enum Mode {
        VIEW, EDIT
    }

    public enum RenderingHint {
        // normal form
        REGULAR(Where.OBJECT_FORMS),

        // inside parent table
        PARENTED_PROPERTY_COLUMN(Where.PARENTED_TABLES),
        PARENTED_TITLE_COLUMN(Where.PARENTED_TABLES),

        // stand alone table
        STANDALONE_PROPERTY_COLUMN(Where.STANDALONE_TABLES),
        STANDALONE_TITLE_COLUMN(Where.STANDALONE_TABLES);

        private final Where where;

        RenderingHint(final Where where) {
            this.where = where;
        }

        public boolean isRegular() {
            return this == REGULAR;
        }

        public boolean isInParentedTable() {
            return this == PARENTED_PROPERTY_COLUMN;
        }

        public boolean isInStandaloneTable() {
            return this == STANDALONE_PROPERTY_COLUMN;
        }

        public boolean isInTable() {
            return isInParentedTable() || isInStandaloneTable() || isInTableTitleColumn();
        }

        public boolean isInTableTitleColumn() {
            return isInParentedTableTitleColumn() || isInStandaloneTableTitleColumn();
        }

        public boolean isInParentedTableTitleColumn() {
            return this == PARENTED_TITLE_COLUMN;
        }

        public boolean isInStandaloneTableTitleColumn() {
            return this == STANDALONE_TITLE_COLUMN;
        }

        public Where asWhere() {
            return this.where;
        }
    }
    
    public interface HasRenderingHints {

        /**
         * Similar to {@link #mustBeEditable()}, though not called from the same locations.
         *
         * My suspicion is that it amounts to more or less the same set of conditions.
         *
         * @return
         */
        boolean isInlinePrompt();
        
        RenderingHint getRenderingHint();
        void setRenderingHint(RenderingHint renderingHint);
        
        Mode getMode();
        void setMode(Mode mode);
        
        // -- SHORTCUTS
        
        default boolean isViewMode() {
            return getMode() == Mode.VIEW;
        }

        default boolean isEditMode() {
            return getMode() == Mode.EDIT;
        }

        default HasRenderingHints toEditMode() {
            setMode(Mode.EDIT);
            return this;
        }

        default HasRenderingHints toViewMode() {
            setMode(Mode.VIEW);
            return this;
        }

    }
    
}
