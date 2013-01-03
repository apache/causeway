package org.apache.isis.security.shiro.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.util.WebUtils;

public class IsisShiroSecurityManagerThreadLocalBinderFilter implements Filter {

    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServletContext servletContext = filterConfig.getServletContext();
        WebEnvironment webEnvironment = WebUtils.getWebEnvironment(servletContext);
        SecurityManager securityManager = webEnvironment.getSecurityManager();
        ThreadContext.bind(securityManager);
        try {
            doFilter(request, response, chain);
        } finally {
            ThreadContext.unbindSecurityManager();
        }
    }

    @Override
    public void destroy() {
    }

}
