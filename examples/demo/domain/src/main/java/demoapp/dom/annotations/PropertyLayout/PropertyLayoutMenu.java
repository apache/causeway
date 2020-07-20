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

import java.util.Optional;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.value.Blob;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.annotations.PropertyLayout.cssClass.PropertyLayoutCssClassVm;
import demoapp.dom.annotations.PropertyLayout.describedAs.PropertyLayoutDescribedAsVm;
import demoapp.dom.annotations.PropertyLayout.hidden.PropertyLayoutHiddenVm;
import demoapp.dom.annotations.PropertyLayout.hidden.child.PropertyLayoutHiddenChildVm;
import demoapp.dom.annotations.PropertyLayout.labelPosition.PropertyLayoutLabelPositionVm;
import demoapp.dom.annotations.PropertyLayout.multiLine.PropertyLayoutMultiLineVm;
import demoapp.dom.annotations.PropertyLayout.named.PropertyLayoutNamedVm;
import demoapp.dom.annotations.PropertyLayout.navigable.FileNodeVm;
import demoapp.dom.annotations.PropertyLayout.renderDay.PropertyLayoutRenderDayVm;
import demoapp.dom.annotations.PropertyLayout.repainting.PropertyLayoutRepaintingVm;
import demoapp.dom.types.Samples;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.PropertyLayoutMenu")
@Log4j2
public class PropertyLayoutMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-file-code", describedAs = "HTML styling")
    public PropertyLayoutCssClassVm cssClass(){
        return new PropertyLayoutCssClassVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-comment", describedAs = "Descriptions shown as tooltips")
    public PropertyLayoutDescribedAsVm describedAs(){
        return new PropertyLayoutDescribedAsVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-tag", describedAs = "Label positions")
    public PropertyLayoutLabelPositionVm labelPosition(){
        return new PropertyLayoutLabelPositionVm();
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
    @ActionLayout(cssClassFa="fa-signature", describedAs = "Customised label text")
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

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-paint-brush", describedAs = "Performance hint for properties holding unchanging large objects")
    public PropertyLayoutRepaintingVm repainting(){
        val vm = new PropertyLayoutRepaintingVm();
        vm.setEditMe("Modify this field to see if repainting occurs...");
        samples.stream()
                .filter(x -> x.getName().endsWith(".pdf"))
                .findFirst()
                .ifPresent(pdfBlob -> {
                    vm.setPropertyUsingAnnotation(pdfBlob);
                    vm.setPropertyUsingLayout(pdfBlob);
                    vm.setPropertyUsingMetaAnnotation(pdfBlob);
                    vm.setPropertyUsingMetaAnnotationButOverridden(pdfBlob);
                });
        return vm;
    }

    @Inject
    ClockService clockService;

    @Inject
    Samples<Blob> samples;

}
