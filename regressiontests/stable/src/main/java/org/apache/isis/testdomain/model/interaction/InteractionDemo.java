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
package org.apache.isis.testdomain.model.interaction;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.commons.internal.base._Strings;

import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "InteractionDemo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType="regressiontests.InteractionDemo", editing=Editing.ENABLED)
public class InteractionDemo {

    @Property(
            editing = Editing.DISABLED,
            editingDisabledReason = "Disabled for demonstration.")
    @XmlElement @Getter @Setter private String stringDisabled;

    @MemberSupport public List<String> autoCompleteStringDisabled(String search) { return null;}

    @Property
    @PropertyLayout(multiLine=3, labelPosition = LabelPosition.TOP)
    @XmlElement @Getter @Setter private String stringMultiline = "initial";

    // verify, all the parameter supporting methods get picked up

    @MemberSupport public boolean hideStringMultiline() { return false; }
    @MemberSupport public String disableStringMultiline() { return null; }
    @MemberSupport public String validateStringMultiline(String proposeValue) { return null; }
    @MemberSupport public String defaultStringMultiline() { return "default"; }
    @MemberSupport public String[] choicesStringMultiline() { return new String[] {"Hello", "World"}; }

    // -- auto search tests

    @Property
    @XmlElement @Getter @Setter private String string2 = "initial";

    @MemberSupport public List<String> autoCompleteString2(String search) {
        return _Strings.isEmpty(search)
                ? null
                : Stream.of(choicesStringMultiline())
                .filter(s->s.contains(search))
                .collect(Collectors.toList());
    }

}
