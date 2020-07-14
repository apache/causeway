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
public class InteractionDemo_multiEnum {

    @SuppressWarnings("unused")
    private final InteractionDemo holder;
    
    @Model
    public int act(DemoEnum a, DemoEnum b, DemoEnum c) {
        return a.ordinal() * (b.ordinal() + c.ordinal());
    }
    
    // -- PARAM 0
    
    @Model 
    public DemoEnum default0Act() {
        return DemoEnum.values()[1];
    }
    
    // -- PARAM 1
    
    @Model 
    public DemoEnum default1Act() {
        return DemoEnum.values()[2];
    }
    
    // -- PARAM 2
    
    @Model 
    public DemoEnum default2Act() {
        return DemoEnum.values()[3];
    }
}
