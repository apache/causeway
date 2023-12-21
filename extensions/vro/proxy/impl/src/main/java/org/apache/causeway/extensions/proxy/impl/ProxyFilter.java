package org.apache.causeway.extensions.proxy.impl;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class ProxyFilter extends OncePerRequestFilter {
    private final ProxyServer processor;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //      CorsConfiguration corsConfiguration = this.configSource.getCorsConfiguration(request);
        //       boolean isValid = this.processor.processRequest(request, response);
        //       if (isValid && !CorsUtils.isPreFlightRequest(request)) {
        filterChain.doFilter(request, response);
//        }
    }
}