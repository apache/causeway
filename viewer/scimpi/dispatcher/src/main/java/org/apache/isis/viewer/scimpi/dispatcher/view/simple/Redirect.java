package org.apache.isis.viewer.scimpi.dispatcher.view.simple;

import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class Redirect extends AbstractElementProcessor {

    public String getName() {
        return "redirect";
    }

    public void process(Request request) {
        String view = request.getRequiredProperty(VIEW);
        request.getContext().redirectTo(view);
    }

}

