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