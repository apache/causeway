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

@Action
@RequiredArgsConstructor
public class InteractionDemo_multiInt {

    @SuppressWarnings("unused")
    private final InteractionDemo holder;
    
    @Model
    public int act(int a, int b, int c) {
        return a * (b + c);
    }
    
    // -- PARAM 0
    
    @Model 
    public int default0Act() {
        return 2;
    }
    
    @Model
    public int[] choices0Act() {
        return new int[] {1, 2, 3, 4};
    }
    
    // -- PARAM 1
    
    @Model 
    public int default1Act() {
        return 3;
    }
    
    @Model
    public int[] choices1Act() {
        return new int[] {1, 2, 3, 4};
    }
    
    // -- PARAM 2
    
    @Model 
    public int default2Act() {
        return 4;
    }
    
    @Model
    public int[] choices2Act() {
        return new int[] {1, 2, 3, 4};
    }
}
