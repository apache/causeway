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
package demoapp.dom.annotLayout.PropertyLayout;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.value.Blob;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.annotLayout.PropertyLayout.cssClass.PropertyLayoutCssClassVm;
import demoapp.dom.annotLayout.PropertyLayout.describedAs.PropertyLayoutDescribedAsVm;
import demoapp.dom.annotLayout.PropertyLayout.hidden.PropertyLayoutHiddenVm;
import demoapp.dom.annotLayout.PropertyLayout.hidden.child.PropertyLayoutHiddenChildVm;
import demoapp.dom.annotLayout.PropertyLayout.labelPosition.PropertyLayoutLabelPositionVm;
import demoapp.dom.annotLayout.PropertyLayout.multiLine.PropertyLayoutMultiLineVm;
import demoapp.dom.annotLayout.PropertyLayout.named.PropertyLayoutNamedVm;
import demoapp.dom.annotLayout.PropertyLayout.navigable.FileNodeVm;
import demoapp.dom.annotLayout.PropertyLayout.renderDay.PropertyLayoutRenderDayVm;
import demoapp.dom.annotLayout.PropertyLayout.repainting.PropertyLayoutRepaintingVm;
import demoapp.dom.annotLayout.PropertyLayout.typicalLength.PropertyLayoutTypicalLengthVm;
import demoapp.dom.types.Samples;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.PropertyLayoutMenu")
@Log4j2
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class PropertyLayoutMenu {

    final ClockService clockService;
    final Samples<Blob> samples;

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
    @ActionLayout(cssClassFa="fa-glasses", describedAs = "Visibility of properties, eg in tables")
    public PropertyLayoutHiddenVm hidden() {
        val vm = new PropertyLayoutHiddenVm();
        vm.setPropertyHiddenAnywhere("hidden anywhere");
        vm.setPropertyHiddenEverywhere("hidden everywhere");
        vm.setPropertyHiddenNowhereUsingAnnotation("hidden nowhere using annotation");
        vm.setPropertyUsingMetaAnnotation("using meta-annotation");
        vm.setPropertyUsingMetaAnnotationButOverridden("using meta-annotation but overridden");

        vm.getChildren().add(new PropertyLayoutHiddenChildVm("child 1", vm));
        vm.getChildren().add(new PropertyLayoutHiddenChildVm("child 2", vm));
        vm.getChildren().add(new PropertyLayoutHiddenChildVm("child 3", vm));
        return vm;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-tag", describedAs = "Label positions")
    public PropertyLayoutLabelPositionVm labelPosition(){
        return new PropertyLayoutLabelPositionVm();
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

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-ruler-horizontal", describedAs = "Length of text fields")
    public PropertyLayoutTypicalLengthVm typicalLength(){
        val vm = new PropertyLayoutTypicalLengthVm();
        vm.setPropertyUsingAnnotation("abcdefghij");
        vm.setPropertyUsingLayout("abcdefghij");
        vm.setPropertyUsingMetaAnnotation("abcdefghij");
        vm.setPropertyUsingMetaAnnotationButOverridden("abcdefghij");
        return vm;
    }


}
