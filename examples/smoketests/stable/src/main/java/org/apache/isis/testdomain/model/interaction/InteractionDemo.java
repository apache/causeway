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

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.extensions.modelannotation.applib.annotation.Model;

import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "InteractionDemo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType="smoketests.InteractionDemo", editing=Editing.ENABLED)
public class InteractionDemo {

    @Property(
            editing = Editing.DISABLED,
            editingDisabledReason = "Disabled for demonstration.")
    @XmlElement @Getter @Setter private String stringDisabled;
    
    @Property
    @PropertyLayout(multiLine=3, labelPosition = LabelPosition.TOP)
    @XmlElement @Getter @Setter private String stringMultiline;

    // verify, all the parameter supporting methods get picked up
    
    @Model public boolean hideStringMultiline() { return false; }         
    @Model public String disableStringMultiline() { return null; }                           
    @Model public String validateStringMultiline(String proposeValue) { return null; }
    @Model public String defaultStringMultiline() { return "default"; }
    @Model public String[] choicesStringMultiline() { return new String[] {"Hello", "World"}; }          
    @Model public List<String> autoCompleteStringMultiline(String search) { 
        return Stream.of(choicesStringMultiline())
                .filter(s->s.contains(search))
                .collect(Collectors.toList()); 
    }
    
}
