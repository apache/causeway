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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.Component;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.ComponentFactoryScalarAbstract;

/**
 * {@link ComponentFactory} for {@link JavaMathBigDecimalPanel}.
 */
public class JavaMathBigDecimalPanelFactory extends ComponentFactoryScalarAbstract {

    private static final long serialVersionUID = 1L;


    public JavaMathBigDecimalPanelFactory() {
        super(JavaMathBigDecimalPanel.class, java.math.BigDecimal.class);
    }

    @Override
    public Component createComponent(final String id, final ScalarModel scalarModel) {
        BigDecimalConverterWithScale converter = getConverter(scalarModel);
        return new JavaMathBigDecimalPanel(id, scalarModel, converter);
    }


    // //////////////////////////////////////

    private final BigDecimalConverterWithScale converterForNullScale = new BigDecimalConverterWithScale(null);
    private final Map<Integer, BigDecimalConverterWithScale> converterByScale = new ConcurrentHashMap<>();

    private BigDecimalConverterWithScale getConverter(final ScalarModel scalarModel) {
        final Integer scale = scalarModel.getScale();
        if(scale == null) {
            return converterForNullScale;
        }
        BigDecimalConverterWithScale bigDecimalConverter = converterByScale.get(scale);
        if(bigDecimalConverter == null) {
            bigDecimalConverter = new BigDecimalConverterWithScale(scale);
            converterByScale.put(scale, bigDecimalConverter);
        }
        return bigDecimalConverter;
    }

}
