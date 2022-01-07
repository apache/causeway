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
package demoapp.dom.featured.layout.describedAs;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.Collection;
import org.apache.isis.applib.annotations.CollectionLayout;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Editing;
import org.apache.isis.applib.annotations.LabelPosition;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.annotations.ObjectSupport;
import org.apache.isis.applib.annotations.Optionality;
import org.apache.isis.applib.annotations.Parameter;
import org.apache.isis.applib.annotations.ParameterLayout;
import org.apache.isis.applib.annotations.PromptStyle;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.PropertyLayout;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.value.Markup;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.actions.progmodel.assoc.DemoItem;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, logicalTypeName = "demo.Tooltip")
public class DescribedAsVm implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "Tooltip Demo";
    }

    // -- NO ARG

    @Action
    @ActionLayout(
            named = "No Arguments",
            describedAs="This is a no-arg action with a tooltip.")
    public DescribedAsVm noArgAction(){
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(
            named = "No Arguments With Confirm",
            cssClass="btn-danger",
            describedAs="This is a no-arg action with a tooltip and 'are you sure' semantics.")
    public DescribedAsVm noArgActionWithConfirm(){
        return this;
    }

    // -- WITH ARG

    @Action
    @ActionLayout(
            named = "With Arguments",
            describedAs="This is an action with arguments and a tooltip.",
            promptStyle=PromptStyle.DIALOG_MODAL)
    public DescribedAsVm biArgAction(

            @Parameter(optionality=Optionality.MANDATORY)
            @ParameterLayout(
                    named="first",
                    describedAs="This is a mandatory parameter with a tooltip.") final
            String firstArg,

            @Parameter(optionality=Optionality.OPTIONAL)
            @ParameterLayout(
                    named="second",
                    describedAs="This is an optional parameter with a tooltip.") final
            String secondArg

            ){

        return this;
    }

    @MemberSupport public String validateBiArgAction(final String firstArg, final String secondArg) {
        return "always fail for demonstration";
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(
            named = "With Arguments And Confirm",
            describedAs="This is an action with arguments, a tooltip and 'are you sure' semantics.",
            promptStyle=PromptStyle.DIALOG_MODAL)
    public DescribedAsVm biArgActionWithConfirm(

            @Parameter(optionality=Optionality.MANDATORY)
            @ParameterLayout(
                    named="first",
                    describedAs="This is a mandatory parameter with a tooltip.") final
            String firstArg,

            @Parameter(optionality=Optionality.OPTIONAL)
            @ParameterLayout(
                    named="second",
                    describedAs="This is an optional parameter with a tooltip.") final
            String secondArg

            ){

        return this;
    }

    @MemberSupport public String validateBiArgActionWithConfirm(final String firstArg, final String secondArg) {
        return "always fail for demonstration";
    }


    // -- DISABLED

    @Action
    @ActionLayout(
            named="Disabled",
            describedAs="This is a disabled action with a tooltip.")
    public DescribedAsVm disabledAction(){
        return this;
    }

    @MemberSupport public String disableDisabledAction() {
        return "Disabled for demonstration.";
    }

    @Action
    @ActionLayout(
            named="Disabled With Confirm",
            cssClass="btn-danger",
            describedAs="This is a disabled action with a tooltip and 'are you sure' "
                    + "semantics.")
    public DescribedAsVm disabledActionWithConfirmation(){
        return this;
    }

    @MemberSupport public String disableDisabledActionWithConfirmation() {
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
    @Getter private List<DemoItem> collection = new ArrayList<>();

    // -- FIELDSET CONTENT

    @PropertyLayout(labelPosition=LabelPosition.NONE)
    public Markup getText1() {
        return new Markup("Mouse-over above buttons, then tooltips should pop up.");
    }

    @PropertyLayout(labelPosition=LabelPosition.NONE)
    public Markup getText2() {
        return new Markup("Click the above buttons, and mouse-over the action's "
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


}
