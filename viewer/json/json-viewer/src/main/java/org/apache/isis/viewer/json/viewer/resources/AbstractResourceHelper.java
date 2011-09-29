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
package org.apache.isis.viewer.json.viewer.resources;

import org.apache.isis.viewer.json.viewer.ResourceContext;

public class AbstractResourceHelper {

    private final ResourceContext resourceContext;
    private final String selfRef;
    
    public AbstractResourceHelper(ResourceContext resourceContext) {
        this(resourceContext, null);
    }

    public AbstractResourceHelper(ResourceContext resourceContext, String selfRef) {
        this.resourceContext = resourceContext;
        this.selfRef = selfRef;
    }

    public ResourceContext getResourceContext() {
        return resourceContext;
    }
    
    public String getSelfRef() {
        return selfRef;
    }
}
