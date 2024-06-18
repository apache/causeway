package org.apache.causeway.applib.log4j2;

import org.apache.logging.log4j.message.AbstractMessageFactory;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;

/**
 * Enable using
 * <pre>
 * -Dlog4j2.messageFactory=\
 *      org.apache.causeway.applib.log4j2.TruncatingMessageFactory
 * </pre>
 */
public class TruncatingMessageFactory extends AbstractMessageFactory {
    private static final int MAX_LENGTH = 128;

    @Override
    public Message newMessage(String message, Object param) {
        Object[] truncatedParams = truncateParams(new Object[]{param});
        return new ParameterizedMessage(message, truncatedParams);
    }

    @Override
    public Message newMessage(String message, Object param, Object param2) {
        Object[] truncatedParams = truncateParams(new Object[]{param, param2});
        return new ParameterizedMessage(message, truncatedParams);
    }

    @Override
    public Message newMessage(String message, Object param, Object param2, Object param3) {
        Object[] truncatedParams = truncateParams(new Object[]{param, param2, param3});
        return new ParameterizedMessage(message, truncatedParams);
    }

    @Override
    public Message newMessage(String message, Object param, Object param2, Object param3, Object param4) {
        Object[] truncatedParams = truncateParams(new Object[]{param, param2, param3, param4});
        return new ParameterizedMessage(message, truncatedParams);
    }

    @Override
    public Message newMessage(String message, Object param, Object param2, Object param3, Object param4, Object param5) {
        Object[] truncatedParams = truncateParams(new Object[]{param, param2, param3, param4, param5});
        return new ParameterizedMessage(message, truncatedParams);
    }

    @Override
    public Message newMessage(String message, Object param, Object param2, Object param3, Object param4, Object param5, Object param6) {
        Object[] truncatedParams = truncateParams(new Object[]{param, param2, param3, param4, param5, param6});
        return new ParameterizedMessage(message, truncatedParams);
    }

    @Override
    public Message newMessage(String message, Object param, Object param2, Object param3, Object param4, Object param5, Object param6, Object param7) {
        Object[] truncatedParams = truncateParams(new Object[]{param, param2, param3, param4, param5, param6, param7});
        return new ParameterizedMessage(message, truncatedParams);
    }

    @Override
    public Message newMessage(String message, Object param, Object param2, Object param3, Object param4, Object param5, Object param6, Object param7, Object param8) {
        Object[] truncatedParams = truncateParams(new Object[]{param, param2, param3, param4, param5, param6, param7, param8});
        return new ParameterizedMessage(message, truncatedParams);
    }

    @Override
    public Message newMessage(String message, Object param, Object param2, Object param3, Object param4, Object param5, Object param6, Object param7, Object param8, Object param9) {
        Object[] truncatedParams = truncateParams(new Object[]{param, param2, param3, param4, param5, param6, param7, param8, param9});
        return new ParameterizedMessage(message, truncatedParams);
    }

    @Override
    public Message newMessage(String message, Object param, Object param2, Object param3, Object param4, Object param5, Object param6, Object param7, Object param8, Object param9, Object param10) {
        Object[] truncatedParams = truncateParams(new Object[]{param, param2, param3, param4, param5, param6, param7, param8, param9, param10});
        return new ParameterizedMessage(message, truncatedParams);
    }

    @Override
    public Message newMessage(String message, Object... params) {
        Object[] truncatedParams = truncateParams(params);
        return new ParameterizedMessage(message, truncatedParams);
    }

    private Object[] truncateParams(Object[] params) {
        if (params == null) {
            return null;
        }

        Object[] truncatedParams = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            String paramString = String.valueOf(params[i]);
            if (paramString.length() > MAX_LENGTH) {
                truncatedParams[i] = paramString.substring(0, MAX_LENGTH) + "...";
            } else {
                truncatedParams[i] = paramString;
            }
        }
        return truncatedParams;
    }
}
