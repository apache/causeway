package org.apache.isis.viewer.wicket.ui.panels;

import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.model.IModel;

public abstract class AjaxButtonWithPreSubmitHook extends AjaxButton implements IFormSubmittingComponentWithPreSubmitHook {
    private static final long serialVersionUID = 1L;
    public AjaxButtonWithPreSubmitHook(String id, IModel<String> model) {
        super(id, model);
    }
}