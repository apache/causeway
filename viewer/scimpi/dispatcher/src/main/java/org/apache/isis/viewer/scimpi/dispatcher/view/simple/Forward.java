package org.apache.isis.viewer.scimpi.dispatcher.view.simple;

import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class Forward extends AbstractElementProcessor {

    public String getName() {
        return "forward";
    }

    public void process(Request request) {
        String view = request.getRequiredProperty(VIEW);
        request.getContext().forward(view);
    }

}

