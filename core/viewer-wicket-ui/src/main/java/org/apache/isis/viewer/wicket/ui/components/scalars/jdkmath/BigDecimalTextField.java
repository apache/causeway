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
package org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath;

import java.math.BigDecimal;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldAbstract;

final class BigDecimalTextField extends TextFieldAbstract<BigDecimal> {

    private static final long serialVersionUID = 1L;

    private final BigDecimalConverterWithScale converter;

    BigDecimalTextField(
            final String id, final IModel<BigDecimal> model, final Class<BigDecimal> type,
            final ScalarModel scalarModel,
            final BigDecimalConverterWithScale converter) {
        super(id, model, type, scalarModel);
        this.converter = converter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> IConverter<C> getConverter(Class<C> type) {
        if (type != BigDecimal.class) {
            return super.getConverter(type);
        }
        return (IConverter<C>) getConverterFor(scalarModel);
    }

    @Override
    protected IConverter<BigDecimal> getConverterFor(ScalarModel scalarModel) {
        if(scalarModel.isEditMode()) {
            return converter.forEditMode();
        } else {
            return converter.forViewMode();
        }
    }
}