package org.apache.isis.examples.onlinedemo.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter attempts to ensure that would-be users of the framework are
 * directed to the bundled documentation, rather than just hitting the
 * REST API (ie the json viewer).
 * 
 * <p>
 * Specifically, if the request is to "/" but the Accept header is anything
 * other than "application/json" (eg is set to "text/html" and suggesting that 
 * the user is using a browser to access the webapp) then the filter 
 * redirects to /index.html (the documentation pages).
 * 
 * <p>
 * Only if the Accept header is set to application/json is the request allowed
 * to continue through.
 */
public class RedirectToDocsFilter implements Filter {
    
    private static final String REDIRECT_TO_KEY = "redirectTo";
    private static final String REDIRECT_TO_DEFAULT = "/index.html";
    
    private static final String ACCEPT_HEADER = "Accept";
    private static final String APPLICATION_JSON_MIME_TYPE = "application/json";
    
    private String redirectTo;

    @Override
    public void init(FilterConfig cfg) throws ServletException {
        redirectTo = cfg.getInitParameter(REDIRECT_TO_KEY);
        if(redirectTo == null) {
            redirectTo = REDIRECT_TO_DEFAULT;
        }
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        
        // do nothing if not mapped to "/"
        if(!"/".equals(httpServletRequest.getServletPath())) {
            chain.doFilter(request, response);
            return;
        }
        
        final String acceptHeader = httpServletRequest.getHeader(ACCEPT_HEADER);
        if(acceptHeader != null && acceptHeader.startsWith(APPLICATION_JSON_MIME_TYPE)) {
            // let request through
            chain.doFilter(request, response);
            return;
        }
        
        // otherwise redirect
        httpServletResponse.sendRedirect(redirectTo);
    }

    @Override
    public void destroy() {
    }
    
}
