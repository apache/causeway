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
package org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;

import java.io.Serializable;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;

import org.springframework.lang.Nullable;

import lombok.Builder;

/**
 * Acts as a rendering hint for the title column in tables.
 * <p>
 * Can be installed and looked up on the {@link Component}(s) involved
 * (EntityLinkSimplePanel and EntityIconAndTitlePanel).
 *
 * @since 2.0.0
 */
@lombok.Value @Builder
public class ColumnAbbreviationOptions implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private final int maxElementTitleLength = -1;

    private static final MetaDataKey<ColumnAbbreviationOptions> KEY = new MetaDataKey<>() {
        private static final long serialVersionUID = 1L; };

    public <T extends Component> T applyTo(final T component) {
        component.setMetaData(KEY, this);
        return component;
    }

    public static Optional<ColumnAbbreviationOptions> lookupIn(final @Nullable Component component) {
        return Optional.ofNullable(component)
                .map(comp->comp.getMetaData(KEY));
    }

}
