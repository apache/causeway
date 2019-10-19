package org.apache.isis.core.webapp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.isis.core.tracing.Scope2;
import org.apache.isis.core.tracing.Span2;
import org.apache.isis.core.tracing.TraceScopeManager;

public class OpenTracingFilterForceClose implements Filter {

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    /**
     * To avoid infinite loops, this is the maximum number of scopes that can be forcibly closed
     */
    private final static int MAX_NESTING_LEVEL = 100;

    @Override
    public void doFilter(
            final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException {

        filterChain.doFilter(servletRequest, servletResponse);

        forceCloseAnyUnclosedScopes();
    }

    private static void forceCloseAnyUnclosedScopes() {
        Scope2 scope2 = TraceScopeManager.get().activeScope();
        int count = 0;
        while(scope2 != null && !scope2.isClosed() && count++ < MAX_NESTING_LEVEL) {

            final Span2 span2 = TraceScopeManager.get().activeSpan();
            if(span2 != null && !span2.isFinished()) {
                span2.log("TracingFilterForceClose")
                        .finish();
            }
            scope2.close();

            scope2 = TraceScopeManager.get().activeScope();
        }
    }

    @Override public void destroy() {
    }
}
