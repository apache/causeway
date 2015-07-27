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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import java.util.EnumMap;

import org.apache.isis.applib.annotation.ActionSemantics;

public final class InvokeKeys {
    
    private final static EnumMap<ActionSemantics.Of, String> map = new EnumMap<ActionSemantics.Of, String>(ActionSemantics.Of.class);
    static {
        map.put(ActionSemantics.Of.SAFE_AND_REQUEST_CACHEABLE, "invokeQueryOnly");
        map.put(ActionSemantics.Of.SAFE, "invokeQueryOnly");
        map.put(ActionSemantics.Of.IDEMPOTENT, "invokeIdempotent");
        map.put(ActionSemantics.Of.NON_IDEMPOTENT, "invoke");
    }
    
    private InvokeKeys() {
    }
    
    public static String getKeyFor(ActionSemantics.Of actionSemantics) {
        return map.get(actionSemantics);
    }
}
