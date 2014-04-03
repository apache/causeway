package org.apache.isis.core.webapp.diagnostics;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simply logs the URL of any request that causes an exception to be thrown.
 */
public class IsisLogOnExceptionFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(IsisLogOnExceptionFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (IOException e) {
            logRequestUrl(request, e);
            throw e;
        } catch (ServletException e) {
            logRequestUrl(request, e);
            throw e;
        } catch (RuntimeException e) {
            logRequestUrl(request, e);
            throw e;
        }
    }

    private static void logRequestUrl(ServletRequest request, Exception e) {
        if(!(request instanceof HttpServletRequest)) {
            return;
        } 
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final StringBuffer buf = httpServletRequest.getRequestURL();
        final String queryString = httpServletRequest.getQueryString();
        if(queryString != null) {
            buf.append("?" + queryString);
        }
        
        LOG.error("Request caused " + e.getClass().getName() + ": " + buf.toString());
    }
}
