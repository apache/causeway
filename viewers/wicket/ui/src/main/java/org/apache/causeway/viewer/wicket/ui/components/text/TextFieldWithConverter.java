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
package org.apache.causeway.viewer.wicket.ui.components.text;

import java.util.Optional;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.validation.IValidationError;

import org.springframework.lang.Nullable;

import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.NonNull;

/**
 * Also properly handles absence of a {@link IConverter}.
 */
public class TextFieldWithConverter<T> extends TextField<T> {

    private static final long serialVersionUID = 1L;

    private final @Nullable IConverter<T> converter;

    public TextFieldWithConverter(
            final @NonNull String id,
            final @NonNull IModel<T> model,
            final @NonNull Class<T> type,
            final @NonNull Optional<IConverter<T>> converter) {
        super(id, model, type);
        this.converter = converter.orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <C> IConverter<C> getConverter(final Class<C> cType) {
        return converter!=null
                && getType().isAssignableFrom(cType)
                ? (IConverter<C>) converter
                : super.getConverter(cType);
    }

    @Override
    public final void error(final IValidationError error) {
        if(!Wkt.errorMessageIgnoringResourceBundles(this, error)) {
            super.error(error); // fallback to original behavior
        }
    }

    @Override
    public boolean checkRequired() {
        return super.checkRequired();
    }

}
