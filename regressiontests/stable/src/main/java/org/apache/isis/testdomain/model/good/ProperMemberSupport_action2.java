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
package org.apache.isis.testdomain.model.good;

import java.util.Set;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Dispatching;
import org.apache.isis.extensions.modelannotation.applib.annotation.Model;

import lombok.RequiredArgsConstructor;

@Mixin @RequiredArgsConstructor
public class ProperMemberSupport_action2 {
    
    private final ProperMemberSupport holder;

    // proper mix-in action
    @Action(executionDispatch = Dispatching.ENABLED) 
    public ProperMemberSupport $$(String p0, String p1) {
        return holder;
    }
    
    @Model
    public String disable$$() {
        return null;
    }
    
    @Model
    public boolean hide$$() {
        return false;
    }

    @Model
    public String validate$$(String p0, String p1) {
        return null;
    }
    
    @Model
    public Set<String> autoComplete0$$(@MinLength(3) String search) {
        return null;
    }

    @Model
    public Set<String> autoComplete1$$(@MinLength(3) String search) {
        return null;
    }

// variant with dependent arg
//    @Model
//    public Set<String> autoComplete1$$(String p0, @MinLength(3) String search) {
//        return null;
//    }
    
    @Model
    public Set<String> choices0$$() {
        return null;
    }

    @Model
    public Set<String> choices1$$() {
        return null;
    }

// variant with dependent arg    
//    @Model 
//    public Set<String> choices1$$(String p0) {
//        return null;
//    }
    
    @Model
    public String default0$$() {
        return null;
    }

    @Model
    public String default1$$() {
        return null;
    }

    @Model
    public String validate0$$(String p0) {
        return null;
    }

    @Model
    public String validate1$$(String p1) {
        return null;
    }

    
}
