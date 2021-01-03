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
package demoapp.dom.ui.custom.vm;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Title;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.ui.custom.latlng.LatLng;
import demoapp.dom.ui.custom.latlng.LatLngUtils;
import demoapp.dom.ui.custom.latlng.Latitude;
import demoapp.dom.ui.custom.latlng.Longitude;
import demoapp.dom.ui.custom.latlng.PositiveNumber;

@XmlRootElement(name = "demo.CustomUiVm")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.CustomUiVm")
public class CustomUiVm implements HasAsciiDocDescription, Serializable {

    @Title
    @Getter @Setter
    private String address;

    @Latitude
    @Getter @Setter
    private String latitude;

    @Longitude
    @Getter @Setter
    private String longitude;

    @PositiveNumber
    @Getter @Setter
    private int scale;

    /**
     * @link https://wiki.openstreetmap.org/wiki/Bounding_Box
     */
    public BoundingBox getBoundingBox() {
        String minLat = LatLngUtils.add(getLatitude(), -getScale());
        String maxLat = LatLngUtils.add(getLatitude(), +getScale());
        String minLng = LatLngUtils.add(getLongitude(), -getScale());
        String maxLng = LatLngUtils.add(getLongitude(), +getScale());
        return new BoundingBox(new LatLng(minLat, minLng), new LatLng(maxLat, maxLng));
    }

}
