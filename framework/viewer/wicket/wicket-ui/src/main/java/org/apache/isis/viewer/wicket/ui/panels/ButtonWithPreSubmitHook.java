package org.apache.isis.viewer.wicket.ui.panels;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.IModel;

public abstract class ButtonWithPreSubmitHook extends Button implements IFormSubmitterWithPreSubmitHook {
    private static final long serialVersionUID = 1L;
    public ButtonWithPreSubmitHook(String id, IModel<String> model) {
        super(id, model);
    }
}