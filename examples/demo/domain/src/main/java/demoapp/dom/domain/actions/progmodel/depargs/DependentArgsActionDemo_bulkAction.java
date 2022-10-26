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
package demoapp.dom.domain.actions.progmodel.depargs;

import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.debug._Probe;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@Action(choicesFrom = "items")
@ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
@RequiredArgsConstructor
public class DependentArgsActionDemo_bulkAction {

    @Inject MessageService messageService;

    private final DependentArgsActionDemo holder;

    @Value @Accessors(fluent = true) // fluent so we can replace this with Java(14+) records later
    static class Parameters {
        Set<DemoItem> demoItems;
        int a;
        Integer b;
        int c;
        int d;
    }

    public DependentArgsActionDemo act(

            // BULK
            Set<DemoItem> demoItems,

            // PARAM 1
            @Parameter(optionality = Optionality.MANDATORY)
            int a,

            // PARAM 2
            @Parameter(optionality = Optionality.OPTIONAL)
            Integer b,

            // PARAM 3
            @Parameter(optionality = Optionality.OPTIONAL)
            int c,

            // PARAM 4
            @Parameter(optionality = Optionality.MANDATORY)
            int d

            ) {

        messageService.informUser("found a fixed point");
        return holder;
    }

    // -- PARAM 1

    @MemberSupport public int default1Act(Parameters params) {
        _Probe.sysOut("p0: %d %d %d %d", params.a, params.b, params.c, params.d);
        return 1;
    }

    // -- PARAM 2

    @MemberSupport public Integer default2Act(Parameters params) {
        _Probe.sysOut("p1: %d %d %d %d", params.a, params.b, params.c, params.d);
        return null;
    }

    // -- PARAM 3

    @MemberSupport public Collection<Integer> choices3Act(Parameters params) {
        return _Lists.of(1,2,3,4);
    }

    @MemberSupport public String validate3Act(Parameters params) {
        return params.c() < 2 ? "please specify c>=2" : null;
    }

//    @Model
//    public int default3Act(Parameters params) {
//        _Probe.sysOut("p2: %d %d %d %d", params.a, params.b, params.c, params.d);
//        return params.b() + 1;
//    }

    // -- PARAM 4

    @MemberSupport public int default4Act(Parameters params) {
        _Probe.sysOut("p3: %d %d %d %d", params.a, params.b, params.c, params.d);
        return params.c() + 1;
    }

//TODO not yet supported
//    @Model
//    public String validate3Act(Parameters params) {
//
//        int cPlusD = params.c()+params.d();
//
//        return (cPlusD%2 == 1)
//                ? "c plus d must be even"
//                : null;
//    }


    @MemberSupport public String validateAct(
            Set<DemoItem> demoItems,
            int a,
            Integer b,
            int c,
            int d) {
        return "just failing always";
    }


}

