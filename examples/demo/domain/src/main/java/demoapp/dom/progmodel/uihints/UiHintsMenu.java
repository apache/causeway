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
package demoapp.dom.progmodel.uihints;

import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;

@Named("demo.UiHintsMenu")
@DomainService(
        nature = NatureOfService.VIEW
)
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
public class UiHintsMenu {

    @Action
    @ActionLayout(cssClassFa="fa-css3", describedAs="cssClass()")
    public Object cssClassSupportingMethod(){
        return null;
    }
    public String disableCssClassSupportingMethod(){
        return "Not yet implemented";
    }

    @Action
    @ActionLayout(cssClassFa="fa-icons", describedAs="icon()")
    public Object iconSupportingMethod(){
        return null;
    }
    public String disableIconSupportingMethod(){
        return "Not yet implemented";
    }

    @Action
    @ActionLayout(cssClassFa="fa-border-all", describedAs="layout()")
    public Object layoutSupportingMethod(){
        return null;
    }
    public String disableLayoutSupportingMethod(){
        return "Not yet implemented";
    }


}
