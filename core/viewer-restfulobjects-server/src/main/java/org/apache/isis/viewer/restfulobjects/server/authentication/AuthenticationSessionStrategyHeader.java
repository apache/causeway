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
package org.apache.isis.viewer.restfulobjects.server.authentication;

import java.util.Collections;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;
import org.apache.isis.core.webapp.auth.AuthenticationSessionStrategyAbstract;

/**
 * Implements a home-grown protocol, whereby the user id and roles are passed
 * using custom headers.
 * 
 * <p>
 * Does not bind the {@link AuthenticationSession} onto the {@link HttpSession}.
 */
public class AuthenticationSessionStrategyHeader extends AuthenticationSessionStrategyAbstract {

    @Override
    public AuthenticationSession lookupValid(final ServletRequest servletRequest, final ServletResponse servletResponse) {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final String user = httpServletRequest.getHeader("isis.user");
        final List<String> roles = rolesFrom(httpServletRequest);

        if (Strings.isNullOrEmpty(user)) {
            return null;
        }
        return new SimpleSession(user, roles);
    }

    protected List<String> rolesFrom(final HttpServletRequest httpServletRequest) {
        final String rolesStr = httpServletRequest.getHeader("isis.roles");
        if (rolesStr == null) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(Splitter.on(",").split(rolesStr));
    }
}
