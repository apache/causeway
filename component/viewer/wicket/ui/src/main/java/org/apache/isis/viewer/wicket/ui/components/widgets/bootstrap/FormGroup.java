package org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap;

import de.agilecoders.wicket.core.util.Attributes;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 *
 */
public class FormGroup extends WebMarkupContainer {

    private final FormComponent<?> formComponent;

    public FormGroup(String id, FormComponent<?> formComponent) {
        super(id);

        this.formComponent = formComponent;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);

        if (!formComponent.isValid()) {
            Attributes.addClass(tag, "has-error");
        }
    }
}
