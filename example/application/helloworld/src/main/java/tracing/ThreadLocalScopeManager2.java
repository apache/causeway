package tracing;

import java.util.concurrent.Callable;

import com.google.common.base.Throwables;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

/**
 * This allows the closing of the scope to be in a different method to the one that activated it
 * (though it must of course be in the same thread).
 */
public class ThreadLocalScopeManager2 implements ScopeManager {

    final ThreadLocal<ThreadLocalScope2> tlsScope = new ThreadLocal<ThreadLocalScope2>();

    @Override
    public Scope activate(Span span) {
        return new ThreadLocalScope2(this, span);
    }

    @Override
    public Span activeSpan() {
        ThreadLocalScope2 scope = activeScope();
        return scope == null ? null : scope.span();
    }

    public ThreadLocalScope2 activeScope() {
        return tlsScope.get();
    }

    public static ThreadLocalScopeManager2 get() {
        return (ThreadLocalScopeManager2) GlobalTracer.get().scopeManager();
    }

    public ThreadLocalScope2 childSpan(final String name) {

        final Span outer = this.activeSpan();
        final Tracer tracer = GlobalTracer.get();
        final Span innerSpan = tracer.buildSpan(name).asChildOf(outer).start();

        return (ThreadLocalScope2) tracer.activateSpan(innerSpan);
    }

    private static ThreadLocalScope2 closeSpanIfAny() {

        final ThreadLocalScopeManager2 scopeManager = (ThreadLocalScopeManager2) GlobalTracer.get().scopeManager();
        final ThreadLocalScope2 scope = scopeManager.activeScope();

        if(scope != null) {
            scope.closeAndFinish();
        }

        return scope;
    }

    public void runInSpan(final String name, final Runnable runnable) {
        ThreadLocalScope2 tracingScope = ThreadLocalScopeManager2.get().childSpan(name);
        try {
            runnable.run();
        } finally {
            tracingScope.closeAndFinish();
        }
    }

    public <T> T callInSpan(final String name, final Callable<T> callable) throws Exception {
        ThreadLocalScope2 tracingScope = ThreadLocalScopeManager2.get().childSpan(name);
        try {
            return callable.call();
        } finally {
            tracingScope.closeAndFinish();
        }
    }

    public <T> T callInSpanEx(final String name, final Callable<T> callable) {
        ThreadLocalScope2 tracingScope = ThreadLocalScopeManager2.get().childSpan(name);
        try {
            return callable.call();
        } catch (Exception e) {
            tracingScope.span().setTag("exception", Throwables.getStackTraceAsString(e));
            return null;
        } finally {
            tracingScope.closeAndFinish();
        }
    }
}
