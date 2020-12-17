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
package demoapp.dom.ui.custom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@XmlRootElement(name = "demo.CustomUiVm")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.CustomUiVm")
public class CustomUiVm implements HasAsciiDocDescription {

    public String title() {
        return "Custom UI";
    }

    @Latitude
    @Getter @Setter
    private String latitude;

    @Longitude
    @Getter @Setter
    private String longitude;

    @PositiveNumber
    @Getter @Setter
    private int scale;

    @Data
    public static class BoundingBox {
        @Getter
        private final String minimumLatitude;
        @Getter
        private final String minimumLongitude;
        @Getter
        private final String maximumLatitude;
        @Getter
        private final String maximumLongitude;

        public final String toUrl(String divider) {
            return getMinimumLongitude() + divider + getMinimumLatitude() + divider + getMaximumLongitude() + divider + getMaximumLatitude();
        }
        public final String toUrl() {
            return toUrl("%2C");
        }
    }
    /**
     * @link https://wiki.openstreetmap.org/wiki/Bounding_Box
     */
    public BoundingBox getBoundingBox() {
        String minLat = LatLng.add(getLatitude(), -getScale());
        String maxLat = LatLng.add(getLatitude(), +getScale());
        String minLng = LatLng.add(getLongitude(), -getScale());
        String maxLng = LatLng.add(getLongitude(), +getScale());
        return new BoundingBox(minLat, minLng, maxLat, maxLng);
    }

}
