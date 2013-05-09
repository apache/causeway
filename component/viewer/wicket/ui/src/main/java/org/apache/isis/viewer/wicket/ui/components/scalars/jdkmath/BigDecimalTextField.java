package org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath;

import java.math.BigDecimal;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;

final class BigDecimalTextField extends TextField<BigDecimal> {
    
        private final ScalarModel model;
        private static final long serialVersionUID = 1L;

        BigDecimalTextField(String id, IModel<BigDecimal> model, Class<BigDecimal> type, ScalarModel model2) {
            super(id, model, type);
            this.model = model2;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <C> IConverter<C> getConverter(Class<C> type) {

            final Integer scale = model.getScale();
            return type == BigDecimal.class 
                    ? (IConverter<C>) new BigDecimalConverter(scale) 
                    : super.getConverter(type);
        }
    }