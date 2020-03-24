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
package demoapp.dom.tooltip;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.incubator.model.applib.annotation.Model;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.actions.assoc.DemoItem;
import demoapp.utils.DemoStub;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.Tooltip")
public class TooltipDemo extends DemoStub {

    @Override
    public String title() {
        return "Tooltip Demo";
    }

    // -- NO ARG

    @Action
    @ActionLayout(describedAs="This is a no-arg action with a tooltip.")
    public TooltipDemo noArgAction(){
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(
            cssClass="btn-danger",
            describedAs="This is a no-arg action with a tooltip and 'are you sure' semantics.")
    public TooltipDemo noArgActionWithConfirm(){
        return this;
    }

    // -- WITH ARG

    @Action
    @ActionLayout(
            describedAs="This is an action with arguments and a tooltip.",
            promptStyle=PromptStyle.DIALOG_MODAL)
    public TooltipDemo biArgAction(

            @Parameter(optionality=Optionality.MANDATORY)
            @ParameterLayout(
                    named="first",
                    describedAs="This is a mandatory parameter with a tooltip.") 
            String firstArg,

            @Parameter(optionality=Optionality.OPTIONAL)
            @ParameterLayout(
                    named="second",
                    describedAs="This is an optional parameter with a tooltip.") 
            String secondArg

            ){

        return this;
    }
    
    @Action(semantics=SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(
            describedAs="This is an action with arguments, a tooltip and 'are you sure' semantics.",
            promptStyle=PromptStyle.DIALOG_MODAL)
    public TooltipDemo biArgActionWithConfirm(

            @Parameter(optionality=Optionality.MANDATORY)
            @ParameterLayout(
                    named="first",
                    describedAs="This is a mandatory parameter with a tooltip.") 
            String firstArg,

            @Parameter(optionality=Optionality.OPTIONAL)
            @ParameterLayout(
                    named="second",
                    describedAs="This is an optional parameter with a tooltip.") 
            String secondArg

            ){

        return this;
    }
    

    // -- DISABLED

    @Action
    @ActionLayout(
            named="Disabled Action", // metamodel validation is picky when method prefix 'disabled' is used
            describedAs="This is a disabled action with a tooltip.")
    public TooltipDemo disabledAction(){
        return this;
    }

    @Model
    public String disableDisabledAction() {
        return "Disabled for demonstration.";
    }

    @Action
    @ActionLayout(
            named="Disabled Action with Confirmation", // metamodel validation is picky when method prefix 'disabled' is used
            cssClass="btn-danger",
            describedAs="This is a disabled action with a tooltip and 'are you sure' "
                    + "semantics.")
    public TooltipDemo disabledActionWithConfirmation(){
        return this;
    }

    @Model
    public String disableDisabledActionWithConfirmation() {
        return "Disabled for demonstration.";
    }

    // -- PROPERTIES

    @Property(editing=Editing.ENABLED)
    @PropertyLayout(describedAs="This is a property with a tooltip.")
    @Getter @Setter private String stringProperty;

    @Property
    @PropertyLayout(describedAs="This is a readonly property with a tooltip.")
    @Getter private String readonlyStringProperty = "readonly";

    @Property(
            editing=Editing.DISABLED, 
            editingDisabledReason="Editing disabled for demonstration.")
    @PropertyLayout(describedAs="This is a 'editing-disabled' property with a tooltip.")
    @Getter @Setter private String editingDisabledStringProperty = "editing disabled";

    // -- COLLECTION

    @Collection
    @CollectionLayout(
            describedAs="This is a collection with a tooltip. Also note, you can mouse-over the "
                    + "'Name' column's head label.")
    @PropertyLayout(labelPosition=LabelPosition.TOP)
    @Getter private List<DemoItem> collection;

    // -- FIELDSET CONTENT

    @PropertyLayout(labelPosition=LabelPosition.NONE)
    public Markup getText1() {
        return new Markup("Mouse-over above buttons, then tooltips should pop up.");
    }

    @PropertyLayout(labelPosition=LabelPosition.NONE)
    public Markup getText2() {
        return new Markup("Click the above button, and mouse-over then action's "
                + "parameter lables. Tooltips should pop up.");
    }

    @PropertyLayout(labelPosition=LabelPosition.NONE)
    public Markup getText3() {
        return new Markup("Mouse-over above buttons, then the 'disabled reason' "
                + "should pop up.");
    }

    @PropertyLayout(labelPosition=LabelPosition.NONE)
    public Markup getText4() {
        return new Markup("Mouse-over below property lables, then tooltips should "
                + "pop up.<p>Mouse-over the disabled/readonly property value form-field,"
                + " then the 'editing disabled reason' should pop up.</p>");
    }

    // ---

    @Override @Programmatic
    public void initDefaults() {

        collection = new ArrayList<>();
        collection.add(DemoItem.of("first"));
        collection.add(DemoItem.of("second"));
        collection.add(DemoItem.of("third"));

    }

}
