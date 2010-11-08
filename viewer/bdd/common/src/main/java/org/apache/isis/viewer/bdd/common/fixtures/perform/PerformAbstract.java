package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.runtime.context.IsisContext;

public abstract class PerformAbstract implements Perform {

    private final String key;
    private final Perform.Mode mode;

    public PerformAbstract(final String key, final Perform.Mode mode) {
        this.key = key;
        this.mode = mode;
    }

    public String getKey() {
        return key;
    }

    protected Perform.Mode getMode() {
        return mode;
    }

    protected AuthenticationSession getSession() {
        return IsisContext.getAuthenticationSession();
    }

}
