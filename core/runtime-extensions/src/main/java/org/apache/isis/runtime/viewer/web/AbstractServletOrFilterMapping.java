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

package org.apache.isis.runtime.viewer.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractServletOrFilterMapping {
    private final Class<?> servletOrFilterClass;
    private final List<String> pathSpecs;
    private final Map<String, String> initParams;

    @SuppressWarnings("unchecked")
    public AbstractServletOrFilterMapping(final Class<?> servletOrFilterClass, final String... pathSpecs) {
        this(servletOrFilterClass, Collections.EMPTY_MAP, pathSpecs);
    }

    public AbstractServletOrFilterMapping(final Class<?> servletOrFilterClass, final Map<String, String> initParams, final String... pathSpecs) {
        this.servletOrFilterClass = servletOrFilterClass;
        this.initParams = initParams;
        this.pathSpecs = new ArrayList<String>(Arrays.asList(pathSpecs));
    }

    protected Class<?> getServletOrFilterClass() {
        return servletOrFilterClass;
    }

    public Map<String, String> getInitParams() {
        return Collections.unmodifiableMap(initParams);
    }

    public List<String> getPathSpecs() {
        return Collections.unmodifiableList(pathSpecs);
    }

}
