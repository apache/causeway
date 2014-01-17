package org.apache.isis.viewer.wicket.ui.actionresponse;

import org.apache.wicket.Component;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.isis.core.runtime.system.context.IsisContext;

public enum ActionResultResponseHandlingStrategy {
    REDIRECT_TO_PAGE {
        @Override
        public void handleResults(final Component component, final ActionResultResponse resultResponse) {
            // force any changes in state etc to happen now prior to the redirect;
            // in the case of an object being returned, this should cause our page mementos 
            // (eg EntityModel) to hold the correct state.  I hope.
            IsisContext.getTransactionManager().flushTransaction();
            
            // "redirect-after-post"
            component.setResponsePage(resultResponse.getToPage());
        }
    },
    SCHEDULE_HANDLER {
        @Override
        public void handleResults(final Component component, final ActionResultResponse resultResponse) {
            RequestCycle requestCycle = component.getRequestCycle();
            requestCycle.scheduleRequestHandlerAfterCurrent(resultResponse.getHandler());
        }
    };

    public abstract void handleResults(Component component, ActionResultResponse resultResponse);

    public static ActionResultResponseHandlingStrategy determineFor(final ActionResultResponse resultResponse) {
        if(resultResponse.isToPage()) {
            return REDIRECT_TO_PAGE;
        } else {
            return SCHEDULE_HANDLER;
        }
    }
}