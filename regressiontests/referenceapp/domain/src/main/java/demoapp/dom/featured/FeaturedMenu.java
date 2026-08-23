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
package demoapp.dom.featured;

import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.factory.FactoryService;

import lombok.RequiredArgsConstructor;

import demoapp.dom.featured.customui.GeoapifyClient;
import demoapp.dom.featured.customui.WhereInTheWorldPage;
import demoapp.dom.featured.customui.Zoom;
import demoapp.dom.featured.layout.tooltip.DemoItem;
import demoapp.dom.featured.layout.tooltip.TooltipPage;

@Named("demo.FeaturedMenu")
@DomainService
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class FeaturedMenu {

    final FactoryService factoryService;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-comment",
            describedAs="Opens the Tooltip-Demo page."
    )
    public TooltipPage toolTips(){
        var demo = factoryService.viewModel(new TooltipPage());

        demo.getCollection().add(DemoItem.of("first"));
        demo.getCollection().add(DemoItem.of("second"));
        demo.getCollection().add(DemoItem.of("third"));

        return demo;
    }

//tag::whereInTheWorldAction[]
    @Inject private GeoapifyClient geoapifyClient;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
        cssClassFa="fa-globe",
        describedAs="Opens a Custom UI page displaying a map for the provided address"
    )
    public WhereInTheWorldPage whereInTheWorld(
            final String address,
            @Zoom final int zoom) {                                     // <.>
        final WhereInTheWorldPage page = new WhereInTheWorldPage();

        final GeoapifyClient.GeocodeResponse response = geoapifyClient.geocode(address);
        page.setAddress(address);
        page.setLatitude(response.getLatitude());
        page.setLongitude(response.getLongitude());
        page.setZoom(zoom);

        return page;
    }
//end::whereInTheWorldAction[]
    @MemberSupport public List<String> choices0WhereInTheWorld() {
        return Arrays.asList("Malvern, UK", "Vienna, Austria", "Leeuwarden, Netherlands", "Dublin, Ireland");
    }
    @MemberSupport public String default0WhereInTheWorld() {
        return "Malvern, UK";
    }
    @MemberSupport public int default1WhereInTheWorld() {
        return 14;
    }
}
