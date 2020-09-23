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
package org.apache.isis.tooling.c4.test;

import java.nio.charset.StandardCharsets;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Text;

final class Util {
    
    // -- RESOURCE LOADING

    public static Can<String> readResource(Class<?> resourceLocation, String resourceName) {
        return _Text.readLinesFromResource(resourceLocation, resourceName, StandardCharsets.UTF_8);  
    }
    
    public static Can<String> readResource(Object resourceLocation, String resourceName) {
        return readResource(resourceLocation.getClass(), resourceName);  
    }
    

    
}
