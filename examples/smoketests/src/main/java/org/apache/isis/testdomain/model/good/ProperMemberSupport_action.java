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
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.incubator.model.applib.annotation.Model;

import lombok.RequiredArgsConstructor;

@Action 
@ActionLayout(named = "foo", describedAs = "bar")  
@RequiredArgsConstructor
public class ProperMemberSupport_action {
    
    private final ProperMemberSupport holder;

    // proper mix-in action
    //@Action // <-- inferred by annotation on type above
    public ProperMemberSupport act(String p0, String p1) {
        return holder;
    }
    
    @Model
    public String disableAct() {
        return null;
    }
    
    @Model
    public boolean hideAct() {
        return false;
    }

    @Model
    public String validateAct(String p0, String p1) {
        return null;
    }
    
    @Model
    public Set<String> autoComplete0Act(@MinLength(3) String search) {
        return null;
    }

    @Model
    public Set<String> autoComplete1Act(@MinLength(3) String search) {
        return null;
    }
    
//    @Model
//    public Set<String> autoComplete1Act(String p0, @MinLength(3) String search) {
//        return null;
//    }
    
    @Model
    public Set<String> choices0Act() {
        return null;
    }

    @Model
    public Set<String> choices1Act(String p0) {
        return null;
    }
    
    @Model
    public String default0Act() {
        return null;
    }

    @Model
    public String default1Act() {
        return null;
    }

    @Model
    public String validate0Act(String p0) {
        return null;
    }

    @Model
    public String validate1Act(String p1) {
        return null;
    }

    
}
