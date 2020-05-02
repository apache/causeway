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
package demoapp.dom.actions.depargs;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.incubator.model.applib.annotation.Model;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@ActionLayout(named="Default (Fixed Point Search)", promptStyle = PromptStyle.DIALOG_MODAL)
@Action
@RequiredArgsConstructor
public class DependentArgsActionDemo_useDefaultFixedPoint {

    @Inject MessageService messageService;

    private final DependentArgsActionDemo holder;
    
    @Value @Accessors(fluent = true) // fluent so we can replace this with Java(14+) records later
    static class Parameters {
        int a;
        int b;
        int c;
        int d;
    }

    public DependentArgsActionDemo act(

            // PARAM 0
            @Parameter(optionality = Optionality.MANDATORY)
            int a,

            // PARAM 1
            @Parameter(optionality = Optionality.MANDATORY)
            int b,
            
            // PARAM 2
            @Parameter(optionality = Optionality.MANDATORY)
            int c,
            
            // PARAM 3
            @Parameter(optionality = Optionality.MANDATORY)
            int d

            ) {

        messageService.informUser("found a fixed point");
        return holder;
    }
    
    // -- PARAM 0

    @Model
    public int default0Act(Parameters params) {
        return 1;
    }

    @Model
    public int default1Act(Parameters params) {
        return params.a() + 1;
    }
    
    @Model
    public int default2Act(Parameters params) {
        return params.b() + 1;
    }
    
    @Model
    public int default3Act(Parameters params) {
        return params.c() + 1;
    }

}

