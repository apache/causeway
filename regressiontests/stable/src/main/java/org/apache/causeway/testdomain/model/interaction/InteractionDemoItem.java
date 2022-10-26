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
package org.apache.causeway.testdomain.model.interaction;

import java.io.Serializable;
import java.time.LocalDate;

import javax.inject.Named;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@DomainObject(nature=Nature.VIEW_MODEL)
@Named("testdomain.InteractionDemoItem")
@NoArgsConstructor
@AllArgsConstructor(staticName="of")
@EqualsAndHashCode @ToString
public class InteractionDemoItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @ObjectSupport public String title() {
        return String.format("DemoItem '%s'", getName());
    }

    @Property(editing = Editing.DISABLED)
    @PropertyLayout(describedAs="The name of this 'DemoItem'.", sequence = "1.0")
    @Getter @Setter private String name;

    @Property(editing = Editing.DISABLED)
    @PropertyLayout(describedAs="The date of this 'DemoItem'.", sequence = "2.0")
    @Getter @Setter private LocalDate date;

}
