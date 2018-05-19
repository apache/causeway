package org.apache.isis.commons.plugins;

public class PluginResolveException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public PluginResolveException(final String msg) {
        super(msg);
    }

    public PluginResolveException(final Throwable cause) {
    	super(cause);
    }
    
    public PluginResolveException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
