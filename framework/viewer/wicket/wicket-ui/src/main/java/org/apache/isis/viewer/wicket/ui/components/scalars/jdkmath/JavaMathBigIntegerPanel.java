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

import java.math.BigInteger;
import java.util.Locale;

import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.AbstractIntegerConverter;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldNumeric;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldValueModel;

/**
 * Panel for rendering scalars of type {@link BigInteger}.
 */
public class JavaMathBigIntegerPanel extends ScalarPanelTextFieldNumeric<BigInteger> {

    private static final long serialVersionUID = 1L;
    private static final String ID_SCALAR_VALUE = "scalarValue";
    
    private static final IConverter CONVERTER = new AbstractIntegerConverter() {
        private static final long serialVersionUID = 1L;
        @Override
        public Object convertToObject(String value, Locale locale) {
            return new BigInteger(value);
        }
        @Override
        protected Class<?> getTargetType() {
            return BigInteger.class;
        }
    };

    public JavaMathBigIntegerPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel, BigInteger.class);
    }

    @Override
    protected AbstractTextComponent<BigInteger> createTextField() {
        return new TextField<BigInteger>(ID_SCALAR_VALUE, new TextFieldValueModel<BigInteger>(this), BigInteger.class) {
            private static final long serialVersionUID = 1L;

//            @Override
//            public IConverter getConverter(Class<?> type) {
//                return type == BigInteger.class? CONVERTER: super.getConverter(type);
//            }
            
            @Override
            public <C> IConverter<C> getConverter(Class<C> type) {
                @SuppressWarnings("unchecked")
                final IConverter<C> converter = (IConverter<C>) new AbstractIntegerConverter<BigInteger>() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public BigInteger convertToObject(String value, Locale locale) {
                        return new BigInteger(value);
                    }
                    @Override
                    protected Class<BigInteger> getTargetType() {
                        return BigInteger.class;
                    }
                };
                return type == BigInteger.class? converter: super.getConverter(type);
            }
        };
    }

}
