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

import java.util.Collection;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.extensions.modelannotation.applib.annotation.Model;

import lombok.Value;
import lombok.experimental.Accessors;

@DomainObject(nature = Nature.VIEW_MODEL)
public class ProperParameterSupport {

    
    @Value @Accessors(fluent = true) // fluent so we can replace this with Java(14+) records later
    static class Parameters {
        String p0;
        int p1;
        boolean p2;
    }
    
    @Action
    public void act(
            String p0,
            int p1,
            boolean p2) {
    }

    // -- PARAM 0 (String)
    
    @Model
    public String default0Act(Parameters p) {
        return null;
    }
    
    @Model
    public Collection<String> choices0Act(Parameters p) {
        return null;
    }
    
    @Model
    public Collection<String> autoComplete0Act(Parameters p, String search) {
        return null;
    }
    
    @Model
    public boolean hide0Act(Parameters p) {
        return false;
    }
    
    @Model
    public String disable0Act(Parameters p) {
        return null;
    }
    
    // -- PARAM 1 (int)
    
    @Model
    public int default1Act(Parameters p) {
        return 0;
    }
    
    @Model
    public int[] choices1Act(Parameters p) {
        return null;
    }
    
    @Model
    public int[] autoComplete1Act(Parameters p, String search) {
        return null;
    }
    
    @Model
    public boolean hide1Act(Parameters p) {
        return false;
    }
    
    @Model
    public String disable1Act(Parameters p) {
        return null;
    }
    
    // -- PARAM 2 (boolean)
    
    @Model
    public boolean default2Act(Parameters p) {
        return false;
    }
    
    @Model
    public boolean[] choices2Act(Parameters p) {
        return null;
    }
    
    @Model
    public boolean[] autoComplete2Act(Parameters p, String search) {
        return null;
    }
    
    @Model
    public boolean hide2Act(Parameters p) {
        return false;
    }
    
    @Model
    public String disable2Act(Parameters p) {
        return null;
    }
    
    
}
