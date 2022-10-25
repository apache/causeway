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
package demoapp.dom.domain.properties.PropertyLayout;

import java.time.ZoneId;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;

import demoapp.dom.domain.properties.PropertyLayout.cssClass.PropertyLayoutCssClassVm;
import demoapp.dom.domain.properties.PropertyLayout.describedAs.PropertyLayoutDescribedAsVm;
import demoapp.dom.domain.properties.PropertyLayout.hidden.PropertyLayoutHiddenVm;
import demoapp.dom.domain.properties.PropertyLayout.hidden.child.PropertyLayoutHiddenChildVm;
import demoapp.dom.domain.properties.PropertyLayout.labelPosition.PropertyLayoutLabelPositionVm;
import demoapp.dom.domain.properties.PropertyLayout.multiLine.PropertyLayoutMultiLineVm;
import demoapp.dom.domain.properties.PropertyLayout.named.PropertyLayoutNamedVm;
import demoapp.dom.domain.properties.PropertyLayout.navigable.FileNodeVm;
import demoapp.dom.domain.properties.PropertyLayout.renderDay.PropertyLayoutRenderDayVm;
import demoapp.dom.domain.properties.PropertyLayout.repainting.PropertyLayoutRepaintingVm;
import demoapp.dom.domain.properties.PropertyLayout.typicalLength.PropertyLayoutTypicalLengthVm;
import demoapp.dom.types.Samples;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Named("demo.PropertyLayoutMenu")
@DomainService(
        nature=NatureOfService.VIEW
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
//@Log4j2
public class PropertyLayoutMenu {

    final ClockService clockService;
    final Samples<Blob> samples;


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-pen-nib", describedAs = "CSS class to wrap the UI component representing this property")
    public PropertyLayoutCssClassVm cssClass(){
        return new PropertyLayoutCssClassVm();
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-comment", describedAs = "Description of the property, shown as a tooltip")
    public PropertyLayoutDescribedAsVm describedAs(){
        return new PropertyLayoutDescribedAsVm();
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-glasses", describedAs = "Visibility of the property in different contexts")
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
    @ActionLayout(cssClassFa="fa-signature", describedAs = "Custom text for the property's label")
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
        return new PropertyLayoutRenderDayVm(clockService.getClock().nowAsLocalDate(ZoneId.systemDefault()));
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-paint-brush", describedAs = "Performance hint for properties holding unchanging large objects")
    public PropertyLayoutRepaintingVm repainting(){
        val vm = new PropertyLayoutRepaintingVm();
        vm.setEditMe("Modify this field to see if repainting occurs...");
        samples.stream()
                .filter(x -> CommonMimeType.PDF.matches(x.getMimeType()))
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
