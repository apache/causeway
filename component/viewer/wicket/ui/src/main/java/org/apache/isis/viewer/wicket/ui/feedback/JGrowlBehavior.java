package org.apache.isis.viewer.wicket.ui.feedback;

import org.apache.commons.lang.StringUtils;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

/**
 * Attach to any component to display jGrowl messages.
 * 
 * Displays only session-level messages. If you need component-level messages,
 * see http://pastebin.com/f6db2ec0e for an example. Basically, instead of
 * Session.get().getFeedbackMessages(), you would call
 * getComponent().getFeedbackMessage().
 * 
 * Requires the following be included: "jquery.js", "jquery.ui.all.js",
 * "jquery.jgrowl.js", "jquery.jgrowl.css". These can be downloaded from
 * http://plugins.jquery.com/files/jGrowl-1.2.0.tgz.
 * 
 * @author jsinai Based on an example by Alex Objelean, see the above link.
 */
public class JGrowlBehavior extends AbstractDefaultAjaxBehavior {
    
    private static final long serialVersionUID = 1L;

    /**
     * Displays an info message that is sticky. The default is non-sticky.
     * Sample usage: session.getFeedbackMessages().add(new FeedbackMessage(null,
     * "my message", JGrowlBehavior.INFO_STICKY));
     */
    public static final int INFO_STICKY = 250;

    @Override
    protected void respond(AjaxRequestTarget target) {
        final String feedbackMsg = renderFeedback();
        if (!StringUtils.isEmpty(feedbackMsg)) {
            target.appendJavaScript(feedbackMsg);
        }
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        final String feedbackMsg = renderFeedback();
        if (!StringUtils.isEmpty(feedbackMsg)) {
            response.render(OnDomReadyHeaderItem.forScript(feedbackMsg));
        }
    }

    private String renderFeedback() {

        final StringBuilder buf = new StringBuilder();
        
        for (String info : IsisContext.getMessageBroker().getMessages()) {
            addJGrowlCall(info, "INFO", false, buf);
        }

        for (String warning : IsisContext.getMessageBroker().getWarnings()) {
            addJGrowlCall(warning, "WARNING", true, buf);
        }
        
        try {
            final String error = ActionModel.applicationError.get();
            if(error!=null) {
                    addJGrowlCall(error, "ERROR", true, buf);
            }
        } finally {
            ActionModel.applicationError.remove();
        }

        return buf.toString();
    }

    void addJGrowlCall(final String msg, final String cssClassSuffix, boolean sticky, final StringBuilder buf) {
        buf.append("$.jGrowl(\"").append(msg).append('\"');
        buf.append(", {");
        buf.append("theme: \'jgrowl-").append(cssClassSuffix).append("\'");
        if (sticky) {
            buf.append(", sticky: true");
        }
        buf.append("}");
        buf.append(");");
    }

    boolean isSticky(final FeedbackMessage message) {
        return message.getLevel() > FeedbackMessage.INFO;
    }

    String messageFor(final FeedbackMessage message) {
        return (message.getMessage() == null) ? StringUtils.EMPTY : message.getMessage().toString();
    }

    String levelFor(final FeedbackMessage message) {
        return (message.getLevel() == INFO_STICKY) ? "INFO" : message.getLevelAsString();
    }
}