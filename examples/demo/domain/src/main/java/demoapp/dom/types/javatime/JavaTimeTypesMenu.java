
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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom.types.javatime.javatimelocaldate.JavaTimeLocalDates;
import demoapp.dom.types.javatime.javatimelocaldatetime.JavaTimeLocalDateTimes;
import demoapp.dom.types.javatime.javatimelocaltime.JavaTimeLocalTimes;
import demoapp.dom.types.javatime.javatimeoffsetdatetime.JavaTimeOffsetDateTimes;
import demoapp.dom.types.javatime.javatimeoffsettime.JavaTimeOffsetTimes;
import demoapp.dom.types.javatime.javatimezoneddatetime.JavaTimeZonedDateTimes;

@DomainService(
        nature=NatureOfService.VIEW,
        logicalTypeName = "demo.JavaTimeTypesMenu"
)
@DomainObjectLayout(
        named="JavaTimeTypes"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class JavaTimeTypesMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-clock")
    public JavaTimeLocalTimes localTimes(){
        return new JavaTimeLocalTimes();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-calendar")
    public JavaTimeLocalDates localDates(){
        return new JavaTimeLocalDates();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-calendar")
    public JavaTimeLocalDateTimes localDateTimes(){
        return new JavaTimeLocalDateTimes();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-calendar")
    public JavaTimeOffsetDateTimes offsetDateTimes(){
        return new JavaTimeOffsetDateTimes();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-clock")
    public JavaTimeOffsetTimes offsetTimes(){
        return new JavaTimeOffsetTimes();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-calendar")
    public JavaTimeZonedDateTimes zonedDateTimes(){
        return new JavaTimeZonedDateTimes();
    }


}
