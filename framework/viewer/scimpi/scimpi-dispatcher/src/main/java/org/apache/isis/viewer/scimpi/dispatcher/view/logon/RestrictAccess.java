package org.apache.isis.viewer.scimpi.dispatcher.view.logon;

import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class RestrictAccess extends AbstractElementProcessor {
    private static final String LOGIN_VIEW = "login-view";
    private static final String DEFAULT_LOGIN_VIEW = "login." + Dispatcher.EXTENSION;

    public String getName() {
        return "restrict-access";
    }

    public void process(Request request) {
        if (!request.getContext().isUserAuthenticated()) { 
            final String view = request.getOptionalProperty(LOGIN_VIEW, DEFAULT_LOGIN_VIEW);
            request.getContext().redirectTo(view);
        }
    }

}

