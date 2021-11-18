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
package org.apache.isis.viewer.wicket.ui.components.scalars.bookmark;

import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.viewer.wicket.model.converter.BookmarkConverterWkt;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldAbstract;

import lombok.NonNull;

/**
 * Panel for rendering scalars of type {@link Bookmark}.
 */
public class BookmarkPanel
extends ScalarPanelTextFieldAbstract<Bookmark> {

    private static final long serialVersionUID = 1L;

    public BookmarkPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel, Bookmark.class);
    }

    @Override
    protected IConverter<Bookmark> getConverter(
            final @NonNull ObjectFeature propOrParam,
            final @NonNull ScalarRepresentation scalarRepresentation) {
        return new BookmarkConverterWkt(propOrParam, scalarRepresentation);
    }

}
