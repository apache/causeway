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
package demoapp.dom.types.jodatime.jodalocaldate.holder;

import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.applib.annotation.Where;

//tag::class[]
public interface JodaLocalDateHolder3 extends JodaLocalDateHolder2 {

    @Property
    @ValueSemantics(dateRenderAdjustDays = -1)                    // <.>
    @PropertyLayout(
            describedAs = "@PropertyLayout(renderDay=AS_DAY_BEFORE)",
            labelPosition = LabelPosition.TOP,
            hidden = Where.ALL_TABLES,
            fieldSetId = "render-day", sequence = "1")            // <.>
    default org.joda.time.LocalDate getReadOnlyPropertyDerivedRenderDayAsDayBefore() {
        return getReadOnlyProperty();
    }

    @Property
    @ValueSemantics(dateRenderAdjustDays = 0)                     // <.>
    @PropertyLayout(
            describedAs = "@ValueSemantics(dateRenderAdjustDays = 0)",
            labelPosition = LabelPosition.TOP,
            hidden = Where.ALL_TABLES,
            fieldSetId = "render-day", sequence = "2")
    default org.joda.time.LocalDate getReadOnlyPropertyDerivedRenderDayAsDay() {
        return getReadOnlyProperty();
    }

    @Property
    @ValueSemantics                                               // <.>
    @PropertyLayout(
            describedAs = "@ValueSemantics",
            labelPosition = LabelPosition.TOP,
            hidden = Where.ALL_TABLES,
            fieldSetId = "render-day", sequence = "3")
    default org.joda.time.LocalDate getReadOnlyPropertyDerivedRenderDayNotSpecified() {
        return getReadOnlyProperty();
    }


}
//end::class[]
