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

import org.apache.wicket.util.convert.IConverter;

final class BigDecimalConverter implements IConverter<BigDecimal> {
    private final Integer scale;
    private static final long serialVersionUID = 1L;

    BigDecimalConverter(Integer scale) {
        this.scale = scale;
    }

    @Override
    public BigDecimal convertToObject(String valueStr, Locale locale) {
        try {
            final BigDecimal value = new BigDecimal(valueStr);
            return scale != null? value.setScale(scale): value;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String convertToString(BigDecimal value, Locale locale) {
        return value.toPlainString();
    }
}