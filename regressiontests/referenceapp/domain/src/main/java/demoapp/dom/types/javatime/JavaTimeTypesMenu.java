
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
package demoapp.dom.types.javatime;

import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom.types.javatime.javatimelocaldate.LocalDates;
import demoapp.dom.types.javatime.javatimelocaldatetime.LocalDateTimes;
import demoapp.dom.types.javatime.javatimelocaltime.LocalTimes;
import demoapp.dom.types.javatime.javatimeoffsetdatetime.OffsetDateTimes;
import demoapp.dom.types.javatime.javatimezoneddatetime.ZonedDateTimes;

@Named("demo.JavaTimeTypesMenu")
@DomainService
@DomainObjectLayout(
        named="JavaTimeTypes"
)
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
public class JavaTimeTypesMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-clock")
    public LocalTimes localTimes(){
        return new LocalTimes();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-calendar")
    public LocalDates localDates(){
        return new LocalDates();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-calendar")
    public LocalDateTimes localDateTimes(){
        return new LocalDateTimes();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-calendar")
    public OffsetDateTimes offsetDateTimes(){
        return new OffsetDateTimes();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-clock")
    public demoapp.dom.types.javatime.javatimeoffsettime.OffsetTimes offsetTimes(){
        return new demoapp.dom.types.javatime.javatimeoffsettime.OffsetTimes();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-calendar")
    public ZonedDateTimes zonedDateTimes(){
        return new ZonedDateTimes();
    }

}
