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
package demoapp.dom.types.javatime.javatimeoffsettime.holder;

import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.TimeZoneTranslation;
import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.applib.annotation.Where;

//tag::class[]
public interface JavaTimeOffsetTimeHolder4 extends JavaTimeOffsetTimeHolder3 {

    @Property
    @ValueSemantics(timeZoneTranslation = TimeZoneTranslation.NONE)
    @PropertyLayout(
            describedAs = "@ValueSemantics(timeZoneTranslation = TimeZoneTranslation.NONE)",
            labelPosition = LabelPosition.TOP,
            hidden = Where.ALL_TABLES,
            fieldSetId = "time-zone-translation", sequence = "1")
    default java.time.OffsetTime getReadOnlyPropertyNoTimeZoneTranslation() {
        return getReadOnlyProperty();
    }

    @Property(editing = Editing.ENABLED)
    @ValueSemantics(timeZoneTranslation = TimeZoneTranslation.NONE)
    @PropertyLayout(
            describedAs = "@ValueSemantics(timeZoneTranslation = TimeZoneTranslation.NONE)",
            labelPosition = LabelPosition.TOP,
            hidden = Where.ALL_TABLES,
            fieldSetId = "time-zone-translation", sequence = "2")
    java.time.OffsetTime getReadWritePropertyNoTimeZoneTranslation();
    void setReadWritePropertyNoTimeZoneTranslation(final java.time.OffsetTime temporal);

}
//end::class[]
