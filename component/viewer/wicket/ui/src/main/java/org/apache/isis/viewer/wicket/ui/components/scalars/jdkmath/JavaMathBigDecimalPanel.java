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
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldNumeric;

/**
 * Panel for rendering scalars of type {@link BigDecimal}.
 */
public class JavaMathBigDecimalPanel extends ScalarPanelTextFieldNumeric<BigDecimal> {

    private static final long serialVersionUID = 1L;

    public JavaMathBigDecimalPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel, BigDecimal.class);
    }
 
    @Override
    protected void addSemantics() {
        super.addSemantics();
    }
    
    protected TextField<BigDecimal> createTextField(final String id) {
        
        final ScalarModel model = getModel();
        
        final TextField<BigDecimal> textField = new TextField<BigDecimal>(id, newTextFieldValueModel(), cls) {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            public <C> IConverter<C> getConverter(Class<C> type) {

                Integer scale = model.getScale();
                final MathContext mathContext = new MathContext(scale+1, RoundingMode.HALF_UP);
                
//                final NumberFormat numberFormat = NumberFormat.getInstance();
//                numberFormat.setMinimumFractionDigits(scale);
//                numberFormat.setMaximumFractionDigits(scale);
//                numberFormat.setMaximumIntegerDigits(model.getLength());

                if(type == BigDecimal.class) {
                    return (IConverter<C>) new IConverter<BigDecimal>() {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public BigDecimal convertToObject(String value, Locale locale) {
                            try {
                                return new BigDecimal(value, mathContext);
//                                final Number parsed = numberFormat.parse(value);
//                                return (BigDecimal) parsed;
                            } catch (Exception e) {
                                return null;
                            }
                        }

                        @Override
                        public String convertToString(BigDecimal value, Locale locale) {
                            //return numberFormat.format(value);
                            return value.toPlainString();
                        }}; 
                } else {
                    return super.getConverter(type);
                }
            }
        };
        
        
        return textField;
    }
    
    


}
