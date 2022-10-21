
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
package demoapp.dom.types.causewayval;

import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom.types.causewayval.asciidocs.CausewayAsciiDocs;
import demoapp.dom.types.causewayval.markdowns.CausewayMarkdowns;
import demoapp.dom.types.causewayval.vegas.CausewayVegas;

@Named("demo.CausewayValTypesMenu")
@DomainService(
        nature=NatureOfService.VIEW
)
@DomainObjectLayout(named="CausewayValTypes")
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class CausewayValTypesMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-pen-fancy")
    public CausewayAsciiDocs asciiDocs(){
        return new CausewayAsciiDocs();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-pen-fancy")
    public CausewayMarkdowns markdowns(){
        return new CausewayMarkdowns();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-chart-gantt")
    public CausewayVegas vegaCharts(){
        return new CausewayVegas();
    }

}
