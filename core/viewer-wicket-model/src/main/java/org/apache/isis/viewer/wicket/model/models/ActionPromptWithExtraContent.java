package org.apache.isis.viewer.wicket.model.models;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * For implementations of {@link ActionPrompt} (eg sidebars) that are also able to display extra content, eg associations of a mixin.
 */
public interface ActionPromptWithExtraContent extends ActionPrompt {

    String getExtraContentId();

    void setExtraContentPanel(final Component extraContentComponent, final AjaxRequestTarget target);

}
