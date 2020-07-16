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
package demoapp.dom.annotations.PropertyLayout;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.annotations.PropertyLayout.cssClass.PropertyLayoutCssClassVm;
import demoapp.dom.annotations.PropertyLayout.describedAs.PropertyLayoutDescribedAsVm;
import demoapp.dom.annotations.PropertyLayout.hidden.PropertyLayoutHiddenVm;
import demoapp.dom.annotations.PropertyLayout.hidden.child.PropertyLayoutHiddenChildVm;
import demoapp.dom.annotations.PropertyLayout.multiLine.PropertyLayoutMultiLineVm;
import demoapp.dom.annotations.PropertyLayout.named.PropertyLayoutNamedVm;
import demoapp.dom.annotations.PropertyLayout.navigable.FileNodeVm;
import demoapp.dom.annotations.PropertyLayout.renderDay.PropertyLayoutRenderDayVm;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.PropertyLayoutMenu")
@Log4j2
public class PropertyLayoutMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-css3", describedAs = "HTML styling")
    public PropertyLayoutCssClassVm cssClass(){
        return new PropertyLayoutCssClassVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-comment", describedAs = "shown as Tooltips")
    public PropertyLayoutDescribedAsVm describedAs(){
        return new PropertyLayoutDescribedAsVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-glasses", describedAs = "Visibility of properties, eg in tables")
    public PropertyLayoutHiddenVm hidden() {
        val vm = new PropertyLayoutHiddenVm();
        vm.getChildren().add(new PropertyLayoutHiddenChildVm("child 1", vm));
        vm.getChildren().add(new PropertyLayoutHiddenChildVm("child 2", vm));
        vm.getChildren().add(new PropertyLayoutHiddenChildVm("child 3", vm));
        return vm;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-align-justify", describedAs = "Textboxes")
    public PropertyLayoutMultiLineVm multiLine(){
        val vm = new PropertyLayoutMultiLineVm();
        vm.setPropertyUsingAnnotation(
                "A multiline string\n" +
                "spanning\n" +
                "5 lines\n" +
                "\n" +
                "(click me to edit)\n"
                );
        vm.setPropertyUsingAnnotationReadOnly(
                "A readonly string\n" +
                "spanning\n" +
                "5 lines\n" +
                "\n" +
                "(Allows text select)");
        return vm;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-signature", describedAs = "Customised labels")
    public PropertyLayoutNamedVm named(){
        return new PropertyLayoutNamedVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-sitemap", describedAs = "Breadcrumbs (and trees)")
    public FileNodeVm navigable(){
        return new FileNodeVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-step-forward", describedAs = "Inclusive and exclusive date ranges")
    public PropertyLayoutRenderDayVm renderDay(){
        return new PropertyLayoutRenderDayVm(clockService.nowAsJodaLocalDate());
    }

    @Inject
    ClockService clockService;

}
