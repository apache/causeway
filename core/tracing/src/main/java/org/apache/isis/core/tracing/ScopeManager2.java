package org.apache.isis.core.tracing;

import io.opentracing.ScopeManager;

public interface ScopeManager2 extends ScopeManager {

    Scope2 activeScope();

    Scope2 startActive(final String name);

    interface Executable {
        void exec(Scope2 scope2);
    }

    interface ExecutableT<T> {
        T exec(Scope2 scope2);
    }
}
