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
package org.apache.isis.testdomain.model.interaction;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.extensions.modelannotation.applib.annotation.Model;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Action
@RequiredArgsConstructor
public class InteractionNpmDemo_biArgEnabled {

    @SuppressWarnings("unused")
    private final InteractionNpmDemo holder;
    
    @Value @Accessors(fluent = true)            
    public static class Parameters {                                   
 
        //@Parameter @ParameterLayout //TODO[ISIS-2362] support these here
        int a;
        //@Parameter @ParameterLayout //TODO[ISIS-2362] support these here
        int b;
    }
    
    
    @Model
    public int act(Parameters params) {
        return params.a() + params.b();
    }
    
    // parameter supporting methods, to be referenced by param name ...
    
    @Model 
    public int defaultA(Parameters params) {
        return 5;
    }
    
    @Model
    public int[] choicesB(Parameters params) {
        return new int[] {1, 2, 3, 4};
    }
}
