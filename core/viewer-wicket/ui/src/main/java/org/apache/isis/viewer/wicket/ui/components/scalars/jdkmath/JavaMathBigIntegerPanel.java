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

import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldNumeric;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldValueModel;

/**
 * Panel for rendering scalars of type {@link BigInteger}.
 */
public class JavaMathBigIntegerPanel extends ScalarPanelTextFieldNumeric<BigInteger> {

    private static final long serialVersionUID = 1L;
    private static final String ID_SCALAR_VALUE = "scalarValue";
    
    public JavaMathBigIntegerPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel, BigInteger.class, BigIntegerConverter.INSTANCE);
    }

    @Override
    protected AbstractTextComponent<BigInteger> createTextFieldForRegular() {
        return new TextField<BigInteger>(ID_SCALAR_VALUE, new TextFieldValueModel<BigInteger>(this), BigInteger.class) {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            public <C> IConverter<C> getConverter(Class<C> type) {
                return (IConverter<C>) (type == BigInteger.class? BigIntegerConverter.INSTANCE: super.getConverter(type));
            }
        };
    }

    @Override
    protected IModel<String> getScalarPanelType() {
        return Model.of("javaMathBigIntegerPanel");
    }

}
