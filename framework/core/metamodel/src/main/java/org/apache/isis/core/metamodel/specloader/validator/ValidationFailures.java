package org.apache.isis.core.metamodel.specloader.validator;

import java.text.MessageFormat;
import java.util.List;

import com.google.common.collect.Lists;

public final class ValidationFailures {

    private final List<String> messages = Lists.newArrayList();
    
    public void add(String pattern, Object... arguments) {
        final String message = MessageFormat.format(pattern, arguments);
        messages.add(message);
    }

    public void assertNone() {
        if (messages.isEmpty()) {
            return;
        }
        
        final StringBuilder buf = new StringBuilder();
        int i=0;
        for (String message : messages) {
            buf.append(++i).append(": ").append(message).append("\n");
        }
        
        throw new MetaModelInvalidException(buf.toString());
    }

}
