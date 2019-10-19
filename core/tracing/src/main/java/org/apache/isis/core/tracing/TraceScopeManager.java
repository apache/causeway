package org.apache.isis.core.tracing;

import java.util.concurrent.Callable;

import com.google.common.base.Throwables;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

/**
 * This allows the closing of the scope to be in a different method to the one that activated it
 * (though it must of course be in the same thread).
 */
public class TraceScopeManager implements ScopeManager2 {

    final ThreadLocal<ThreadLocalScope2> tlsScope = new ThreadLocal<>();

    public static TraceScopeManager get() {
        return (TraceScopeManager) GlobalTracer.get().scopeManager();
    }

    @Override
    public Scope activate(Span span) {
        return new ThreadLocalScope2(this, (Span2)span);
    }

    @Override
    public Span2 activeSpan() {
        Scope2 scope = activeScope();
        return scope == null ? null : scope.span();
    }

    @Override
    public Scope2 activeScope() {
        return tlsScope.get();
    }

    /**
     * To avoid infinite loops, this is the maximum number of scopes ot search to root.
     */
    private final static int MAX_NESTING_LEVEL = 100;

    /**
     * Searches for the scope with no parent.
     */
    public Scope2 rootScope() {
        ThreadLocalScope2 scope2 = tlsScope.get();
        if(scope2 == null) {
            return null;
        }

        int count = 0;
        while(scope2.toRestore != null && count++ < MAX_NESTING_LEVEL) {
            scope2 = scope2.toRestore;
        }
        return scope2;
    }

    public Scope2 startActive(final String name) {

        final Span2 outerSpan = this.activeSpan();

        final Tracer tracer = GlobalTracer.get();
        final JaegerSpan2 innerSpan =
                (JaegerSpan2) tracer.buildSpan(name).asChildOf(outerSpan).start();
        final ThreadLocalScope2 innerScope = (ThreadLocalScope2) tracer.activateSpan(innerSpan);
        innerSpan.setScope(innerScope);

        return innerScope;
    }


    public void execInScope(final String operationName, final Executable withinScope) {
        final Scope2 newScope = TraceScopeManager.get().startActive(operationName);
        final Span2 span = newScope.span();
        span.setTag(Span2.START_TAG, operationName)
                .log(operationName);
        try {
            withinScope.exec(newScope);
        } finally {
            span.setTag(Span2.FINISH_TAG, operationName).finish();
            newScope.close();
        }
    }

    public <T> T execInScope(final String operationName, final ExecutableT<T> withinScope) {
        final Scope2 newScope = TraceScopeManager.get().startActive(operationName);
        final Span2 span = newScope.span();
        span.setTag(Span2.START_TAG, operationName)
                .log(operationName);
        try {
            return withinScope.exec(newScope);
        } finally {
            span.setTag(Span2.FINISH_TAG, operationName).finish();
            newScope.close();
        }
    }

    public void runInScope(final String operationName, final Runnable runnable) {
        final Scope2 newScope = TraceScopeManager.get().startActive(operationName);
        final Span2 span = newScope.span();
        span.setTag(Span2.START_TAG, operationName)
                .log(operationName);
        try {
            runnable.run();
        } finally {
            span.setTag(Span2.FINISH_TAG, operationName).finish();
            newScope.close();
        }
    }

    public <T> T callInScope(final String operationName, final Callable<T> callable) throws Exception {
        final Scope2 tracingScope = TraceScopeManager.get()
                .startActive(operationName);
        final Span2 span = tracingScope.span();
        span.setTag(Span2.START_TAG, operationName)
                .log(operationName);
        try {
            return callable.call();
        } finally {
            span.setTag(Span2.FINISH_TAG, operationName).finish();
            tracingScope.close();
        }
    }

    public <T> T callInScopeX(final String operationName, final Callable<T> callable) {
        final Scope2 tracingScope = TraceScopeManager.get()
                .startActive(operationName);
        final Span2 span = tracingScope.span();
        span.setTag(Span2.START_TAG, operationName)
            .log(operationName);
        try {
            return callable.call();
        } catch (Exception e) {
            span.setTag("exception", Throwables.getStackTraceAsString(e));
            return null;
        } finally {
            span.setTag(Span2.FINISH_TAG, operationName).finish();
            tracingScope.close();
        }
    }

    public void rootOperation(final String operationName) {
        final Scope2 rootScope = rootScope();
        if(rootScope == null || rootScope.span() == null) {
            return;
        }
        final Span2 span = rootScope.span();
        if(span.hasTag(Span2.ROOT_OPERATION_TAG)) {
            return;
        }
        span.setOperationName(operationName);
        span.setTag(Span2.ROOT_OPERATION_TAG, true);
    }
}
