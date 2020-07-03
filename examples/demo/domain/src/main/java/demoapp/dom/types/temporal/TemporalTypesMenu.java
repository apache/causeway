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
package demoapp.dom.types.temporal;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.extern.log4j.Log4j2;

import demoapp.dom.types.temporal.javasqldate.TemporalJavaSqlDates;
import demoapp.dom.types.temporal.javasqltimestamp.TemporalJavaSqlTimestamps;
import demoapp.dom.types.temporal.javatimelocaldate.TemporalJavaTimeLocalDates;
import demoapp.dom.types.temporal.javatimelocaldatetime.TemporalJavaTimeLocalDateTimes;
import demoapp.dom.types.temporal.javatimeoffsetdatetime.TemporalJavaTimeOffsetDateTimes;
import demoapp.dom.types.temporal.javautildate.TemporalJavaUtilDates;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.TemporalTypesMenu")
@DomainObjectLayout(named="TemporalTypes")
@Log4j2
public class TemporalTypesMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-clock")
    public TemporalJavaSqlDates javaSqlDates(){
        return new TemporalJavaSqlDates();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-clock")
    public TemporalJavaTimeLocalDates javaTimeLocalDates(){
        return new TemporalJavaTimeLocalDates();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-clock")
    public TemporalJavaTimeLocalDateTimes javaTimeLocalDateTimes(){
        return new TemporalJavaTimeLocalDateTimes();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-clock")
    public TemporalJavaUtilDates javaUtilDates(){
        return new TemporalJavaUtilDates();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-clock")
    public TemporalJavaSqlTimestamps javaSqlTimestamps(){
        return new TemporalJavaSqlTimestamps();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-clock")
    public TemporalJavaTimeOffsetDateTimes javaTimeOffsetDateTimes(){
        return new TemporalJavaTimeOffsetDateTimes();
    }


}
