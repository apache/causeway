/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.core.webapp.modules.templresources;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.causeway.commons.internal.exceptions._Exceptions.FluentException;

import lombok.val;

public class TemplateResourceCachingFilter implements Filter {

    /**
     * Attribute set on {@link HttpServletRequest} if the filter has been
     * applied.
     *
     * <p>
     * This is intended to inform other filters.
     */
    private static final String REQUEST_ATTRIBUTE =
            TemplateResourceCachingFilter.class.getName() + ".resource";

    /**
     * To allow other filters to ask whether a request is mapped to the resource
     * caching filter.
     *
     * <p>
     * For example, the <tt>CausewayRestfulObjectsInteractionFilter</tt> uses this in order to skip
     * any session handling.
     */
    public static boolean isCachedResource(final HttpServletRequest request) {
        return request.getAttribute(REQUEST_ATTRIBUTE) != null;
    }

    /**
     * The Constant MILLISECONDS_IN_SECOND.
     */
    private static final int MILLISECONDS_IN_SECOND = 1000;

    /** The Constant POST_CHECK_VALUE. */
    private static final String POST_CHECK_VALUE = "post-check=";

    /** The Constant PRE_CHECK_VALUE. */
    private static final String PRE_CHECK_VALUE = "pre-check=";

    /** The Constant MAX_AGE_VALUE. */
    private static final String MAX_AGE_VALUE = "max-age=";

    /** The Constant ZERO_STRING_VALUE. */
    private static final String ZERO_STRING_VALUE = "0";

    /** The Constant NO_STORE_VALUE. */
    private static final String NO_STORE_VALUE = "no-store";

    /** The Constant NO_CACHE_VALUE. */
    private static final String NO_CACHE_VALUE = "no-cache";

    /** The Constant PRAGMA_HEADER. */
    private static final String PRAGMA_HEADER = "Pragma";

    /** The Constant CACHE_CONTROL_HEADER. */
    private static final String CACHE_CONTROL_HEADER = "Cache-Control";

    /** The Constant EXPIRES_HEADER. */
    private static final String EXPIRES_HEADER = "Expires";

    /** The Constant LAST_MODIFIED_HEADER. */
    private static final String LAST_MODIFIED_HEADER = "Last-Modified";

    /** The Constant CACHE_TIME_PARAM_NAME. */
    private static final String CACHE_TIME_PARAM_NAME = "CacheTime";

    /** The default for {@link #CACHE_TIME_PARAM_NAME}. */
    private static final String CACHE_TIME_PARAM_NAME_DEFAULT = "" + 86400;

    /** The reply headers. */
    private String[][] mReplyHeaders = { {} };

    /** The cache time in seconds. */
    private long cacheTime = 0L;

    private static final DateFormat httpDateFormat() {
        // not thread-safe, so each thread should have its own instance
        val dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat;
    }

    /**
     * Initializes the Servlet filter with the cache time and sets up the
     * unchanging headers.
     *
     * @param pConfig
     *            the config
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(final FilterConfig pConfig) {
        final ArrayList<String[]> newReplyHeaders = new ArrayList<String[]>();
        final String cacheTime = pConfig.getInitParameter(CACHE_TIME_PARAM_NAME);
        this.cacheTime = Long.parseLong(cacheTime != null ? cacheTime : CACHE_TIME_PARAM_NAME_DEFAULT);
        if (this.cacheTime > 0L) {
            newReplyHeaders.add(new String[] { CACHE_CONTROL_HEADER, MAX_AGE_VALUE + this.cacheTime });
            newReplyHeaders.add(new String[] { CACHE_CONTROL_HEADER, PRE_CHECK_VALUE + this.cacheTime });
            newReplyHeaders.add(new String[] { CACHE_CONTROL_HEADER, POST_CHECK_VALUE + this.cacheTime });
        } else {
            newReplyHeaders.add(new String[] { PRAGMA_HEADER, NO_CACHE_VALUE });
            newReplyHeaders.add(new String[] { EXPIRES_HEADER, ZERO_STRING_VALUE });
            newReplyHeaders.add(new String[] { CACHE_CONTROL_HEADER, NO_CACHE_VALUE });
            newReplyHeaders.add(new String[] { CACHE_CONTROL_HEADER, NO_STORE_VALUE });
        }
        this.mReplyHeaders = new String[newReplyHeaders.size()][2];
        newReplyHeaders.toArray(this.mReplyHeaders);
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    /**
     * Do filter.
     *
     * @param servletRequest
     *            the request
     * @param servletResponse
     *            the response
     * @param chain
     *            the chain
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ServletException
     *             the servlet exception
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain) throws IOException, ServletException {
        // Apply the headers
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        for (final String[] replyHeader : this.mReplyHeaders) {
            final String name = replyHeader[0];
            final String value = replyHeader[1];
            httpResponse.addHeader(name, value);
        }
        if (this.cacheTime > 0L) {
            final long now = System.currentTimeMillis();
            val dateFormat = httpDateFormat();
            httpResponse.addHeader(LAST_MODIFIED_HEADER, dateFormat.format(new Date(now)));
            httpResponse.addHeader(EXPIRES_HEADER, dateFormat.format(new Date(now + (this.cacheTime * MILLISECONDS_IN_SECOND))));
        }
        httpRequest.setAttribute(REQUEST_ATTRIBUTE, true);

        // try to suppress java.io.IOException of kind 'client connection abort'
        // 1) the TCP protocol (by design) does not provide a means to check, whether a
        //    connection has been closed by the client
        // 2) the exception thrown and the exception message text are specific to the
        //    servlet-engine implementation, so we can only guess here
        try {
            chain.doFilter(servletRequest, servletResponse);
        } catch (IOException e) {
            FluentException.of(e)
            .suppressIf(this::isConnectionAbortException);
        }
    }

    // -- HELPER

    private boolean isConnectionAbortException(IOException e) {
        // tomcat 9
        if(e.getMessage().contains("An established connection was aborted by the software in your host machine")) {
            return true;
        }
        // payara 4
        if(e.getMessage().contains("Connection is closed")) {
            return true;
        }

        return false;
    }



}
