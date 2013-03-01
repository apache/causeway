package org.apache.isis.viewer.wicket.ui.notifications;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;


/**
 * Attach to any Ajax button that might trigger a notification (ie calls
 * {@link MessageBroker#addMessage(String)}, {@link MessageBroker#addWarning(String)},
 * {@link MessageBroker#setApplicationError(String)} or throws an {@link ApplicationException}). 
 * 
 * <p>
 * Attach using the standard Wicket code:
 * <pre>
 * Button editButton = new AjaxButton(ID_EDIT_BUTTON, Model.of("Edit")) { ... }
 * editButton.add(new JGrowlBehaviour());
 * </pre>
 */
public class JGrowlBehaviour extends AbstractDefaultAjaxBehavior {

    private static final long serialVersionUID = 1L;

    @Override
    protected void respond(AjaxRequestTarget target) {
        String feedbackMsg = JGrowlUtil.asJGrowlCalls(IsisContext.getMessageBroker());
        if(!StringUtils.isNullOrEmpty(feedbackMsg)) {
            target.appendJavaScript(feedbackMsg);
        }
    }
    
    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        String feedbackMsg = JGrowlUtil.asJGrowlCalls(IsisContext.getMessageBroker());
        if(!StringUtils.isNullOrEmpty(feedbackMsg)) {
            response.render(OnDomReadyHeaderItem.forScript(feedbackMsg));
        }
    }
}