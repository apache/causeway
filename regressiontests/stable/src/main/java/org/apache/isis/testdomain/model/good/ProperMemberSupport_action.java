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
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Publishing;

import lombok.RequiredArgsConstructor;

@Action(executionPublishing = Publishing.ENABLED) 
@ActionLayout(named = "foo", describedAs = "bar")  
@RequiredArgsConstructor
public class ProperMemberSupport_action {
    
    private final ProperMemberSupport holder;

    // proper mix-in action
    //@Action // <-- inferred by annotation on type above
    public ProperMemberSupport act(String p0, String p1) {
        return holder;
    }
    
    @MemberSupport
    public String disableAct() {
        return null;
    }
    
    @MemberSupport
    public boolean hideAct() {
        return false;
    }

    @MemberSupport
    public String validateAct(String p0, String p1) {
        return null;
    }
    
    @MemberSupport
    public Set<String> autoComplete0Act(@MinLength(3) String search) {
        return null;
    }

    @MemberSupport
    public Set<String> autoComplete1Act(@MinLength(3) String search) {
        return null;
    }
    
//    @MemberSupport
//    public Set<String> autoComplete1Act(String p0, @MinLength(3) String search) {
//        return null;
//    }
    
    @MemberSupport
    public Set<String> choices0Act() {
        return null;
    }

    @MemberSupport
    public Set<String> choices1Act(String p0) {
        return null;
    }
    
    @MemberSupport
    public String default0Act() {
        return null;
    }

    @MemberSupport
    public String default1Act() {
        return null;
    }

    @MemberSupport
    public String validate0Act(String p0) {
        return null;
    }

    @MemberSupport
    public String validate1Act(String p1) {
        return null;
    }

    
}
