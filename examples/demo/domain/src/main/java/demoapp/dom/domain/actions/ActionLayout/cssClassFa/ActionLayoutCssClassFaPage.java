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
package demoapp.dom.domain.actions.ActionLayout.cssClassFa;

import javax.inject.Named;
import javax.xml.bind.annotation.*;

import org.apache.causeway.applib.annotation.*;
import org.apache.causeway.applib.layout.component.CssClassFaPosition;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.Setter;

//tag::class[]
@DomainObject(
        nature=Nature.VIEW_MODEL)
@Named("demo.ActionLayoutCssClassFaVm")
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class ActionLayoutCssClassFaPage implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "@ActionLayout#cssClassFa";
    }

    @Property
    @XmlElement
    @Getter
    @Setter
    private String name;


//tag::actLeftAndRight[]
    @Action
    @ActionLayout(
            cssClassFa = "fa-bus"                         // <.>
            )
    public Object actionWithFaIconOnTheLeft(final String arg) {
        return this;
    }

    @Action
    @ActionLayout(
            cssClassFa = "fa-bus",
            cssClassFaPosition = CssClassFaPosition.RIGHT // <.>
//end::actLeftAndRight[]
            ,describedAs = "@ActionLayout(cssClassFa = \"fa-bus\", \n"
                    + "cssClassFaPosition = CssClassFaPosition.RIGHT)"
//tag::actLeftAndRight[]
            )
    public Object actRight(final String arg) {
        return this;
    }
//end::actLeftAndRight[]

}
//end::class[]
