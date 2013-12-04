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
import java.util.Locale;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldNumeric;

/**
 * Panel for rendering scalars of type {@link BigDecimal}.
 */
public class JavaMathBigDecimalPanel extends ScalarPanelTextFieldNumeric<BigDecimal> {

    private static final long serialVersionUID = 1L;
    
    private final IConverter<BigDecimal> threadSafeConverter;

    public JavaMathBigDecimalPanel(final String id, final ScalarModel scalarModel, final IConverter<BigDecimal> threadSafeConverter) {
        super(id, scalarModel, BigDecimal.class);
        this.threadSafeConverter = threadSafeConverter;
    }
 
    protected TextField<BigDecimal> createTextField(final String id) {
        final ScalarModel model = getModel();
        return new BigDecimalTextField(id, newTextFieldValueModel(), cls, model, threadSafeConverter);
    }
    
    static final class BigDecimalTextField extends TextField<BigDecimal> {
        
        private static final long serialVersionUID = 1L;
        
        private final ScalarModel scalarModel;
        private final IConverter<BigDecimal> threadSafeConverter;

        BigDecimalTextField(
                final String id, final IModel<BigDecimal> model, final Class<BigDecimal> type, 
                final ScalarModel scalarModel, 
                final IConverter<BigDecimal> threadSafeConverter) {
            super(id, model, type);
            this.scalarModel = scalarModel;
            this.threadSafeConverter = threadSafeConverter;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <C> IConverter<C> getConverter(Class<C> type) {
            final Integer scale = scalarModel.getScale();
            if (type != BigDecimal.class) {
                return super.getConverter(type);
            } 
            if (scale == null) {
                return (IConverter<C>) threadSafeConverter;
            } 
            return (IConverter<C>) new ConverterWithScale(scale, threadSafeConverter);
        }
    }
    
    static final class ConverterWithScale implements IConverter<BigDecimal> {
        private static final long serialVersionUID = 1L;
        
        private final Integer scale;
        private final IConverter<BigDecimal> converter;

        ConverterWithScale(Integer scale, IConverter<BigDecimal> threadSafeConverter) {
            this.scale = scale;
            this.converter = threadSafeConverter;
        }

        @Override
        public BigDecimal convertToObject(String valueStr, Locale locale) {
            final BigDecimal bd = converter.convertToObject(valueStr, locale);
            return bd != null ? bd.setScale(this.scale) : null; 
        }

        @Override
        public String convertToString(BigDecimal value, Locale locale) {
            return converter.convertToString(value, locale);
        }
    }
}



