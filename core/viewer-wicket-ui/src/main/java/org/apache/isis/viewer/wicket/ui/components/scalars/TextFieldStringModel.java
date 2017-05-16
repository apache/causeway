package org.apache.isis.viewer.wicket.ui.components.scalars;

import org.apache.wicket.model.Model;

public class TextFieldStringModel extends Model<String> {
    private static final long serialVersionUID = 1L;
    private final TextFieldValueModel.ScalarModelProvider scalarModelProvider;

    public TextFieldStringModel(final TextFieldValueModel.ScalarModelProvider scalarModelProvider) {
        this.scalarModelProvider = scalarModelProvider;
    }

    @Override
    public String getObject() {
        return scalarModelProvider.getModel().getObjectAsString();
    }

    @Override
    public void setObject(final String object) {
        if (object == null) {
            scalarModelProvider.getModel().setObject(null);
        } else {
            scalarModelProvider.getModel().setObjectAsString(object);
        }
    }
}
