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
 *
 */

package org.apache.causeway.regressiontests.layouts.integtest.model;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.layout.LayoutConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement()
@XmlAccessorOrder
@Named("layouts.test.Counter")
@DomainObject(nature = Nature.VIEW_MODEL)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Counter implements Comparable<Counter> {

    @PropertyLayout(fieldSetId = LayoutConstants.FieldSetId.IDENTITY)
    @Getter @Setter
    private String name;

    @PropertyLayout(fieldSetId = LayoutConstants.FieldSetId.DETAILS)
    @Getter @Setter
    private Long num;

    @PropertyLayout(fieldSetId = LayoutConstants.FieldSetId.IDENTITY)
    @Getter @Setter
    private String propertyInFieldSetIdIdentity;

    @PropertyLayout(fieldSetId = LayoutConstants.FieldSetId.DETAILS)
    @Getter @Setter
    private String propertyInFieldSetIdDetails;

    @Action()
    @ActionLayout()
    public Counter actionNoPosition(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(position = ActionLayout.Position.BELOW)
    public Counter actionPositionBelow(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(position = ActionLayout.Position.PANEL)
    public Counter actionPositionPanel(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(position = ActionLayout.Position.PANEL_DROPDOWN)
    public Counter actionPositionPanelDropdown(String newName) {
        return doUpdateName(newName);
    }


    // with details fieldset (which contains at least one property) + sequence

    @Action()
    @ActionLayout(fieldSetId = LayoutConstants.FieldSetId.DETAILS, sequence = "1")
    public Counter actionDetailsFieldSetNoPosition(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(fieldSetId = LayoutConstants.FieldSetId.DETAILS, sequence = "2", position = ActionLayout.Position.BELOW)
    public Counter actionDetailsFieldSetPositionBelow(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(fieldSetId = LayoutConstants.FieldSetId.DETAILS, sequence = "3", position = ActionLayout.Position.PANEL)
    public Counter actionDetailsFieldSetPositionPanel(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(fieldSetId = LayoutConstants.FieldSetId.DETAILS, sequence = "4", position = ActionLayout.Position.PANEL_DROPDOWN)
    public Counter actionDetailsFieldSetPositionPanelDropdown(String newName) {
        return doUpdateName(newName);
    }


    // with empty fieldset (that has no properties) + sequence

    @Action()
    @ActionLayout(fieldSetId = "empty", sequence = "1")
    public Counter actionEmptyFieldSetNoPosition(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(fieldSetId = "empty", sequence = "2", position = ActionLayout.Position.BELOW)
    public Counter actionEmptyFieldSetPositionBelow(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(fieldSetId = "empty", sequence = "3", position = ActionLayout.Position.PANEL)
    public Counter actionEmptyFieldSetPositionPanel(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(fieldSetId = "empty", sequence = "4", position = ActionLayout.Position.PANEL_DROPDOWN)
    public Counter actionEmptyFieldSetPositionPanelDropdown(String newName) {
        return doUpdateName(newName);
    }


    // with associateWith

    @Action()
    @ActionLayout(associateWith = "name")
    public Counter actionAssociatedWithNamePropertyNoPosition(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(associateWith = "name", position = ActionLayout.Position.BELOW)
    public Counter actionAssociatedWithNamePropertyBelow(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(associateWith = "name", position = ActionLayout.Position.PANEL)
    public Counter actionAssociatedWithNamePropertyPanel(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(associateWith = "name", position = ActionLayout.Position.PANEL_DROPDOWN)
    public Counter actionAssociatedWithNamePropertyPanelDropdown(String newName) {
        return doUpdateName(newName);
    }


    // with associateWith + correct fieldSet for the associated property

    @Action()
    @ActionLayout(associateWith = "name", fieldSetId = "details", sequence = "1")
    public Counter actionAssociatedWithNamePropertyAndDetailsFieldSetNoPosition(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(associateWith = "name", fieldSetId = "details", sequence = "2", position = ActionLayout.Position.BELOW)
    public Counter actionAssociatedWithNameAndDetailsFieldSetPropertyBelow(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(associateWith = "name", fieldSetId = "details", sequence = "3", position = ActionLayout.Position.PANEL)
    public Counter actionAssociatedWithNamePropertyAndDetailsFieldSetPanel(String newName) {
        return doUpdateName(newName);
    }

    @Action()
    @ActionLayout(associateWith = "name", fieldSetId = "details", sequence = "4", position = ActionLayout.Position.PANEL_DROPDOWN)
    public Counter actionAssociatedWithNamePropertyAndDetailsFieldSetPanelDropdown(String newName) {
        return doUpdateName(newName);
    }


    // with associateWith + incorrect fieldSet for the associated property

    @Action()
    @ActionLayout(associateWith = "name", fieldSetId = "empty", sequence = "1")
    public Counter actionAssociatedWithNamePropertyButEmptyFieldSetNoPosition(String newName) {
        return doUpdateName(newName);
    }


    // associateWith + only the sequence

    @Action()
    @ActionLayout(associateWith = "name", sequence = "1")
    public Counter actionAssociatedWithNamePropertyAndSequenceNoPosition(String newName) {
        return doUpdateName(newName);
    }



    Counter doUpdateName(String newName) {
        setName(newName);
        return this;
    }

    @Override
    public int compareTo(final Counter o) {
        return this.getName().compareTo(o.getName());
    }
}
