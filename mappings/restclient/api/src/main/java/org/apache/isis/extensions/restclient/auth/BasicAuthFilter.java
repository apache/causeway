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
package org.apache.isis.extensions.restclient.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.xml.bind.DatatypeConverter;


import org.apache.isis.core.commons.internal.base._Strings;

import static org.apache.isis.core.commons.internal.base._With.requires;

/**
 * 
 * @since 2.0
 */
@Priority(100)
public class BasicAuthFilter implements ClientRequestFilter {

    /**
     * 
     * @since 2.0
     */
    public static class Credentials {
        final String user;
        final String pass;
        public static Credentials empty() {
            return new Credentials("anonymous", null);
        }
        public static Credentials of(String user, String pass) {
            if(_Strings.isNullOrEmpty(user)) {
                return empty();
            }
            return new Credentials(user, pass);
        }
        private Credentials(String user, String pass) {
            this.user = user;
            this.pass = pass;
        }
        @Override
        public String toString() {
            return "" + user + ":" + pass;
        }
    }

    public static BasicAuthFilter of(Credentials credentials) {
        BasicAuthFilter filter = new BasicAuthFilter();
        filter.setCredentials(credentials);
        return filter;
    }

    private Credentials credentials = Credentials.empty();

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = requires(credentials, "credentials");
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().add("Authorization", getAuthorizationValue());
    }

    // -- HELPER

    private String getAuthorizationValue() {
        try {
            return "Basic " + DatatypeConverter.printBase64Binary(credentials.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("Cannot encode with UTF-8", ex);
        }
    }

}