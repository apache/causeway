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
    public BigDecimal convertToObject(String value, Locale locale) {
        try {
            return new BigDecimal(value).setScale(scale);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String convertToString(BigDecimal value, Locale locale) {
        return value.toPlainString();
    }
}