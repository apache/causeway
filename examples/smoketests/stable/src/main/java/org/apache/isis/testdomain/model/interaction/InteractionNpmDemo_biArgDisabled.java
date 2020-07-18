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
public class InteractionNpmDemo_biArgDisabled {

    @SuppressWarnings("unused")
    private final InteractionNpmDemo holder;
    
    @Value @Accessors(fluent = true)            
    public static class Parameters {      
        int a;
        int b;
    }
    
    @Model
    public int act(int a, int b) {
        return a + b;
    }
    
    @Model
    public boolean hide() {
        return false;
    }
    
    @Model
    public String disable() {
        return "Disabled for demonstration.";
    }
    
    @Model
    public String validate(Parameters params) {
        return "Never valid for demonstration.";
    }
    
    // -- PARAM SUPPORTING METHODS 
    
    // testing whether all of these get picked up by the meta-model
    
    @Model public boolean hideA(Parameters params) { return false; }         
    @Model public String disableA(Parameters params) { return null; }                           
    @Model public String validateA(Parameters params) { return null; }
    @Model public int[] choicesA(Parameters params) { return null; }          
    @Model public int[] autoCompleteA(Parameters params, String search) { return null; }
    @Model public int defaultA(Parameters params) { return 0; }
    
    @Model public boolean hideB(Parameters params) { return false; }         
    @Model public String disableB(Parameters params) { return null; }                           
    @Model public String validateB(Parameters params) { return null; }
    @Model public int[] choicesB(Parameters params) { return null; }          
    @Model public int[] autoCompleteB(Parameters params, String search) { return null; }
    @Model public int defaultB(Parameters params) { return 0; }
    
}
