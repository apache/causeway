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
package demoapp.dom.types.javalang.longs.holder;

import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import java.lang.Long;

//tag::class[]
public interface WrapperLongHolder2 extends WrapperLongHolder {

    @Property                                               // <.>
    @PropertyLayout(
        labelPosition = LabelPosition.LEFT,                 // <.>
        describedAs =
            "@PropertyLayout(labelPosition=LEFT)",
        hidden = Where.ALL_TABLES,
        fieldSetId = "label-positions", sequence = "1")     // <.>
    default Long getReadOnlyPropertyDerivedLabelPositionLeft() {
        return getReadOnlyProperty();
    }

    @Property
    @PropertyLayout(
        labelPosition = LabelPosition.TOP,                  // <.>
        describedAs =
            "@PropertyLayout(labelPosition=TOP)",
        hidden = Where.ALL_TABLES,
        fieldSetId = "label-positions", sequence = "2")
    default Long getReadOnlyPropertyDerivedLabelPositionTop() {
        return getReadOnlyProperty();
    }

    @Property
    @PropertyLayout(
        labelPosition = LabelPosition.RIGHT,                // <.>
        describedAs =
            "@PropertyLayout(labelPosition=RIGHT)",
        hidden = Where.ALL_TABLES,
        fieldSetId = "label-positions", sequence = "3")
    default Long getReadOnlyPropertyDerivedLabelPositionRight() {
        return getReadOnlyProperty();
    }

    @Property
    @PropertyLayout(
        labelPosition = LabelPosition.NONE,                 // <.>
        describedAs =
            "@PropertyLayout(labelPosition=NONE)",
        hidden = Where.ALL_TABLES,
        fieldSetId = "label-positions", sequence = "4")
    default Long getReadOnlyPropertyDerivedLabelPositionNone() {
        return getReadOnlyProperty();
    }

}
//end::class[]
