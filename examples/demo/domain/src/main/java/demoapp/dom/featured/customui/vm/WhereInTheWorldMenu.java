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
package demoapp.dom.featured.customui.vm;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.val;

import demoapp.dom.featured.customui.geocoding.GeoapifyClient;
import demoapp.dom.featured.customui.latlng.Zoom;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.WhereInTheWorldMenu")
public class WhereInTheWorldMenu {

//tag::action[]
    @Inject
    private GeoapifyClient geoapifyClient;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-globe",
            describedAs="Opens a Custom UI page displaying a map for the provided address"
    )
    public WhereInTheWorldVm whereInTheWorld(
            final String address,
            @Zoom final int zoom
    ){
        val vm = new WhereInTheWorldVm();

        val latLng = geoapifyClient.geocode(address);
        vm.setAddress(address);
        vm.setLatitude(latLng.getLatitude());
        vm.setLongitude(latLng.getLongitude());
        vm.setZoom(zoom);

        return vm;
    }
//end::action[]

    public List<String> choices0WhereInTheWorld() {
        return Arrays.asList("Malvern, UK", "Vienna, Austria", "Leeuwarden, Netherlands", "Dublin, Ireland");
    }
    public String default0WhereInTheWorld() {
        return "Malvern, UK";
    }
    public int default1WhereInTheWorld() {
        return 14;
    }

}
