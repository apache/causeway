package org.apache.isis.viewer.wicket.ui.errors;

import java.io.Serializable;

public class StackTraceDetail implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public static StackTraceDetail exceptionClassName(Throwable cause) {
        return new StackTraceDetail(StackTraceDetail.Type.EXCEPTION_CLASS_NAME, cause.getClass().getName());
    }

    public static StackTraceDetail exceptionMessage(Throwable cause) {
        return new StackTraceDetail(StackTraceDetail.Type.EXCEPTION_MESSAGE, cause.getMessage());
    }

    public static StackTraceDetail element(StackTraceElement el) {
        StringBuilder buf = new StringBuilder();
        buf .append("    ")
            .append(el.getClassName())
            .append("#")
            .append(el.getMethodName())
            .append("(")
            .append(el.getFileName())
            .append(":")
            .append(el.getLineNumber())
            .append(")\n")
            ;
        return new StackTraceDetail(StackTraceDetail.Type.STACKTRACE_ELEMENT, buf.toString());
    }

    enum Type {
        EXCEPTION_CLASS_NAME,
        EXCEPTION_MESSAGE,
        STACKTRACE_ELEMENT
    }
    private final Type type;
    private final String line;
    
    public StackTraceDetail(Type type, String line) {
        this.type = type;
        this.line = line;
    }
    public StackTraceDetail.Type getType() {
        return type;
    }
    public String getLine() {
        return line;
    }

}