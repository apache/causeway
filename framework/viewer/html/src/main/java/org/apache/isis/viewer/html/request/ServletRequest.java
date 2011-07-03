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

package org.apache.isis.viewer.html.request;

import javax.servlet.http.HttpServletRequest;

public class ServletRequest implements Request {
    private Request forwardRequest;
    private final HttpServletRequest request;
    private final String requestType;

    public ServletRequest(final HttpServletRequest request) {
        this.request = request;

        final String path = request.getServletPath();
        final int from = path.lastIndexOf('/');
        final int to = path.lastIndexOf('.');
        requestType = path.substring(from + 1, to);
    }

    @Override
    public void forward(final Request forwardRequest) {
        this.forwardRequest = forwardRequest;
    }

    @Override
    public String getActionId() {
        return request.getParameter("action");
    }

    @Override
    public String getProperty() {
        return request.getParameter("field");
    }

    @Override
    public String getElementId() {
        return request.getParameter("element");
    }

    @Override
    public String getFieldEntry(final int i) {
        return request.getParameter("fld" + i);
    }

    @Override
    public String getTaskId() {
        return request.getParameter("id");
    }

    @Override
    public Request getForward() {
        return forwardRequest;
    }

    @Override
    public String getName() {
        return request.getParameter("name");
    }

    @Override
    public String getObjectId() {
        return request.getParameter("id");
    }

    @Override
    public String getRequestType() {
        return requestType;
    }

    @Override
    public String getButtonName() {
        return request.getParameter("button");
    }

    @Override
    public String toString() {
        return "ServletRequest " + request.getRequestURI() + "?" + request.getQueryString();
    }

}
