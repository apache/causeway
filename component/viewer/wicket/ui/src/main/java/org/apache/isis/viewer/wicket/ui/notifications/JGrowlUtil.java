package org.apache.isis.viewer.wicket.ui.notifications;

import org.apache.isis.core.commons.authentication.MessageBroker;

public class JGrowlUtil {
    
    private JGrowlUtil(){}

    public static String asJGrowlCalls(MessageBroker messageBroker) {
        final StringBuilder buf = new StringBuilder();
        
        for (String info : messageBroker.getMessages()) {
            addJGrowlCall(info, "INFO", false, buf);
        }
        for (String warning : messageBroker.getWarnings()) {
            addJGrowlCall(warning, "WARNING", true, buf);
        }
        
        final String error =  messageBroker.getApplicationError();
        if(error!=null) {
            addJGrowlCall(error, "ERROR", true, buf);
        }
        return buf.toString();
    }

    private static void addJGrowlCall(final String msg, final String cssClassSuffix, boolean sticky, final StringBuilder buf) {
        buf.append("$.jGrowl(\"").append(msg).append('\"');
        buf.append(", {");
        buf.append("theme: \'jgrowl-").append(cssClassSuffix).append("\'");
        if (sticky) {
            buf.append(", sticky: true");
        }
        buf.append("}");
        buf.append(");");
    }


}
