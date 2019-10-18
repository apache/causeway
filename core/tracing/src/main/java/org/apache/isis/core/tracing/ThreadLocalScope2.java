package org.apache.isis.core.tracing;

import io.opentracing.Scope;
import io.opentracing.Span;

public class ThreadLocalScope2 implements Scope {
    private final ThreadLocalScopeManager2 scopeManager;
    private final Span wrapped;
    private final ThreadLocalScope2 toRestore;

    ThreadLocalScope2(ThreadLocalScopeManager2 scopeManager, Span wrapped) {
        this.scopeManager = scopeManager;
        this.wrapped = wrapped;
        this.toRestore = scopeManager.tlsScope.get();
        scopeManager.tlsScope.set(this);
    }


    @Override
    public void close() {
        if (scopeManager.tlsScope.get() != this) {
            // This shouldn't happen if users call methods in the expected order. Bail out.
            return;
        }

        scopeManager.tlsScope.set(toRestore);
    }

    public Span span() {
        return wrapped;
    }

    public void closeAndFinish() {
        close();
        wrapped.finish();
    }
}
