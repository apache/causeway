package org.apache.isis.core.tracing;

class ThreadLocalScope2 implements Scope2 {

    private final TraceScopeManager scopeManager;
    private final Span2 wrapped;
    final ThreadLocalScope2 toRestore;

    ThreadLocalScope2(TraceScopeManager scopeManager, Span2 wrapped) {
        this.scopeManager = scopeManager;
        this.wrapped = wrapped;
        this.toRestore = scopeManager.tlsScope.get();
        scopeManager.tlsScope.set(this);
    }


    @Override
    public void close() {
        if (!isActive()) {
            // This shouldn't happen if users call methods in the expected order. Bail out.
            return;
        }

        scopeManager.tlsScope.set(toRestore);
    }

    public Span2 span() {
        return wrapped;
    }

    public void closeAndFinish() {
        close();
        wrapped.finish();
    }

    @Override public boolean isClosed() {
        return !isActive();
    }

    private boolean isActive() {
        return scopeManager.tlsScope.get() == this;
    }
}
