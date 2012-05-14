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

package org.apache.isis.viewer.scimpi.dispatcher.context;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;

public interface ObjectMapping {

    void reloadIdentityMap();
    void endSession();

    void clear();

    String mapObject(ObjectAdapter obj, Scope scope);
    String mapTransientObject(ObjectAdapter object);
    void unmapObject(ObjectAdapter object, Scope scope);

    ObjectAdapter mappedObject(String oidStr);
    ObjectAdapter mappedTransientObject(String jsonData);

    void append(DebugBuilder debug);
    void appendMappings(DebugBuilder debug);
}
