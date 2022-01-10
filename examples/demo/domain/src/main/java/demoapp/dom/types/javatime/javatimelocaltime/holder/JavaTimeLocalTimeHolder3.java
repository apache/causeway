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
package demoapp.dom.types.javatime.javatimelocaltime.holder;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.RenderDay;
import org.apache.isis.applib.annotation.Where;

//tag::class[]
public interface JavaTimeLocalTimeHolder3 extends JavaTimeLocalTimeHolder2 {

    @Property
    @PropertyLayout(
            renderDay = RenderDay.AS_DAY_BEFORE,                 // <.>
            describedAs = "@PropertyLayout(renderDay=AS_DAY_BEFORE)",
            labelPosition = LabelPosition.TOP,
            hidden = Where.ALL_TABLES,
            fieldSetId = "render-day", sequence = "1")            // <.>
    default java.time.LocalTime getReadOnlyPropertyDerivedRenderDayAsDayBefore() {
        return getReadOnlyProperty();
    }

    @Property
    @PropertyLayout(
            renderDay = RenderDay.AS_DAY,                        // <.>
            describedAs = "@PropertyLayout(renderDay=AS_DAY)",
            labelPosition = LabelPosition.TOP,
            hidden = Where.ALL_TABLES,
            fieldSetId = "render-day", sequence = "2")
    default java.time.LocalTime getReadOnlyPropertyDerivedRenderDayAsDay() {
        return getReadOnlyProperty();
    }

    @Property
    @PropertyLayout(
            renderDay = RenderDay.NOT_SPECIFIED,                // <.>
            describedAs = "@PropertyLayout(renderDay=NOT_SPECIFIED)",
            labelPosition = LabelPosition.TOP,
            hidden = Where.ALL_TABLES,
            fieldSetId = "render-day", sequence = "3")
    default java.time.LocalTime getReadOnlyPropertyDerivedRenderDayNotSpecified() {
        return getReadOnlyProperty();
    }


}
//end::class[]
