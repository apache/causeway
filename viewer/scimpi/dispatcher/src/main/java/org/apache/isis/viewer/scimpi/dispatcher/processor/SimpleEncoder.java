package org.apache.isis.viewer.scimpi.dispatcher.processor;

import org.apache.commons.lang.StringEscapeUtils;

public class SimpleEncoder implements Encoder {

    public String encoder(String text) {
        return  StringEscapeUtils.escapeHtml(text);
//        text.replace("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replace("\"", "&quot;");
    }

}

