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
package org.apache.isis.viewer.wicket.ui.components.scalars.uuid;

import java.util.UUID;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldAbstract;

final class UuidTextField extends TextFieldAbstract<UUID> {

    private static final long serialVersionUID = 1L;

    private final UuidConverter converter;

    UuidTextField(
            final String id, final IModel<UUID> model, final Class<UUID> type,
            final ScalarModel scalarModel,
            final UuidConverter converter) {
        super(id, model, type, scalarModel);
        this.converter = converter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> IConverter<C> getConverter(Class<C> type) {
        if (type != UUID.class) {
            return super.getConverter(type);
        }
        return (IConverter<C>) getConverterFor(scalarModel);
    }

    @Override
    protected IConverter<UUID> getConverterFor(ScalarModel scalarModel) {
        return converter;
    }
}