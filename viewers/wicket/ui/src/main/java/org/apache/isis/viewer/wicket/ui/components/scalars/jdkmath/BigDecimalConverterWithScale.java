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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.AbstractNumberConverter;
import org.apache.wicket.util.convert.converter.BigDecimalConverter;

/**
 * The {@link IConverter} implementation that our {@link BigDecimalTextField} delegates to for converting strings into
 * values.
 *
 * <p>
 * We reuse as much of Wicket's {@link BigDecimal} implementation as possible, but overriding where necessary.
 * Whereas Wicket's own {@link BigDecimalConverter} is (clearly?) intended as a singleton, we actually want multiple
 * instances, per scale.  The {@link JavaMathBigDecimalPanelFactory} actually takes care of handling this cache,
 * providing the {@link JavaMathBigDecimalPanel} with an appropriate underlying converter for it to delegate to.
 */
public class BigDecimalConverterWithScale extends BigDecimalConverter {

    /**
     * For {@link JavaMathBigDecimalPanelFactory} to call, so that there is a single instance.
     */
    static AbstractNumberConverter<BigDecimal> newThreadSafeConverter(final Integer scale) {
        return new BigDecimalConverterWithScale(scale);
    }

    private static final long serialVersionUID = 1L;
    private final Integer scale;

    public BigDecimalConverterWithScale(final Integer scale) {
        this.scale = scale!=null
                && scale>=0
                ? scale
                : null;
    }

    /**
     * Disables thousands separator grouping.
     */
    @Override
    protected NumberFormat newNumberFormat(final Locale locale) {
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        numberFormat.setGroupingUsed(false);
        return numberFormat;
    }

    /**
     * Forces trailing zeros to be rendered.
     */
    @Override
    public NumberFormat getNumberFormat(final Locale locale)
    {
        // we obtain a clone, so is okay to modify it to our purposes.
        NumberFormat numberFormat = super.getNumberFormat(locale);
        if(scale != null) {
            numberFormat.setMaximumFractionDigits(scale);
            numberFormat.setMinimumFractionDigits(scale);
        }
        return numberFormat;
    }

    @Override
    public BigDecimal convertToObject(final String valueStr, final Locale locale) throws ConversionException {

        DecimalFormat numberFormat = (DecimalFormat) getNumberFormat(locale);
        char groupingSeparator = numberFormat.getDecimalFormatSymbols().getGroupingSeparator();

        if(valueStr.contains(""+groupingSeparator)) {
            // TODO: this is not actually shown; we see a generic error
            // need to configure the ConversionException somehow
            throw new ConversionException("Thousands separator '" + groupingSeparator + "' is not allowed in input");
        }

        // could also throw an exception
        final BigDecimal bd = super.convertToObject(valueStr, locale);

        if(this.scale != null) {
            if(bd.scale() > this.scale) {
                // TODO: this is not actually shown; we see a generic error
                // need to configure the ConversionException somehow
                throw new ConversionException("No more than " + this.scale + " digits can be entered after the decimal place");
            }
            try {
                return bd.setScale(this.scale);
            } catch(Exception ex) {
                // TODO: this is not actually shown; we see a generic error
                // need to configure the ConversionException somehow
                throw new ConversionException("'" + valueStr + "' is not a valid decimal number.");
            }
        } else {
            return bd;
        }
    }

    public IConverter<BigDecimal> forEditMode() {
        return this;
    }

    public IConverter<BigDecimal> forViewMode() {
        return new BigDecimalConverterWithScale(this.scale){
            private static final long serialVersionUID = 1L;
            @Override
            public String convertToString(final BigDecimal value, final Locale locale) {
                NumberFormat fmt = BigDecimalConverterWithScale.this.getNumberFormat(locale);
                fmt.setGroupingUsed(true);// re-enable for view mode
                return fmt.format(value);
            }
        };
    }

}
