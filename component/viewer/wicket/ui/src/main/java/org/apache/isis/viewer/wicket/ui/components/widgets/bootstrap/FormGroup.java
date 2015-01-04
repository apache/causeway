package org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap;

import de.agilecoders.wicket.core.util.Attributes;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessages;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.lang.Args;

/**
 * A container around Bootstrap form component that sets
 * <a href="http://getbootstrap.com/css/#forms-control-validation">validation state</a>
 */
public class FormGroup extends WebMarkupContainer {

    private final FormComponent<?> formComponent;

    public FormGroup(String id, FormComponent<?> formComponent) {
        super(id);

        this.formComponent = Args.notNull(formComponent, "formComponent");
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);

        FeedbackMessages feedbackMessages = formComponent.getFeedbackMessages();

        if (feedbackMessages.hasMessage(FeedbackMessage.ERROR)) {
            Attributes.addClass(tag, "has-error");
        } else if (feedbackMessages.hasMessage(FeedbackMessage.WARNING)) {
            Attributes.addClass(tag, "has-warning");
        } else if (feedbackMessages.hasMessage(FeedbackMessage.SUCCESS)) {
            Attributes.addClass(tag, "has-success");
        }
    }
}
