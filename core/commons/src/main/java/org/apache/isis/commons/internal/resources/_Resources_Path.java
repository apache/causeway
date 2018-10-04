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


package org.apache.isis.commons.internal.resources;

import org.apache.isis.commons.internal.base._Strings;

/**
 *
 * package private abstract helper class to store application scoped path objects
 * <br/><br/>
 *
 * Implementation Note: Empty paths are represented by absence of this resource,
 * hence empty paths are not allowed (will throw an IllegalArgumentException on
 * attempted construction)
 *
 */
abstract class _Resources_Path {

    protected final String path;

    protected abstract String resourceName();

    public _Resources_Path(String contextPath) {

        if(_Strings.isEmpty(contextPath))
            throw new IllegalArgumentException(resourceName()+" can not be empty");

        contextPath = contextPath.trim();

        if(_Strings.isEmpty(contextPath))
            throw new IllegalArgumentException(resourceName()+" can not be empty");

        while(contextPath.startsWith("/")) {
            contextPath = contextPath.substring(1);
        }

        while(contextPath.endsWith("/")) {
            contextPath = contextPath.substring(0, contextPath.length()-1);
        }

        contextPath = _Strings.condenseWhitespaces(contextPath, " ");

        if(contextPath.indexOf(' ')>-1)
            throw new IllegalArgumentException(resourceName()+" can not contain any white-spaces");

        this.path = contextPath;
    }

}
