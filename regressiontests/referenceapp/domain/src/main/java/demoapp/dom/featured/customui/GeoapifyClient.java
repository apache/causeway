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
package demoapp.dom.featured.customui;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.inject.Inject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import org.apache.causeway.commons.internal.base._Bytes;

import demoapp.dom.AppConfiguration;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;

//tag::class[]
@Service
public class GeoapifyClient implements Serializable {

//end::class[]
    private final static ObjectMapper objectMapper =
                new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final String apiKey;

    @Inject
    public GeoapifyClient(final AppConfiguration appConfiguration) {
        this.apiKey = appConfiguration.getGeoapify().getApiKey();
    }

//tag::class[]
    @Value
    public class GeocodeResponse {
        String latitude;
        String longitude;
    }

    @SneakyThrows
    public GeocodeResponse geocode(final String address) {
        //...
//end::class[]

        var url = new URL(String.format(
                "https://api.geoapify.com/v1/geocode/search?text=%s&apiKey=%s"
                , URLEncoder.encode(address, StandardCharsets.UTF_8)
                , apiKey));

        var response = objectMapper.readValue(url, Response.class);

        return new GeocodeResponse(
                response.getFeatures().get(0).getProperties().getLat(),
                response.getFeatures().get(0).getProperties().getLon()
        );
//tag::class[]
    }
//end::class[]

    @Data
    @Builder
    public static class JpegRequest {
        String latitude;
        String longitude;
        int zoom;
        @Builder.Default int width = 800;
        @Builder.Default int height = 600;
    }

//tag::class[]
    public byte[] toJpeg(
            final String latitude,
            final String longitude,
            final int zoom) throws IOException {
        //...
//end::class[]
        return toJpeg(JpegRequest.builder().latitude(latitude).longitude(longitude).zoom(zoom).build());
//tag::class[]
    }
//end::class[]

    public byte[] toJpeg(final JpegRequest request) throws IOException {
        var urlStr = String.format(
                "https://maps.geoapify.com/v1/staticmap?style=osm-carto&width=%s&height=%s&center=lonlat:%s,%s&zoom=%d&apiKey=%s"
                , request.getWidth()
                , request.getHeight()
                , request.getLongitude()
                , request.getLatitude()
                , request.getZoom()
                , apiKey);
        var con = (HttpURLConnection) new URL(urlStr).openConnection();
        var is = con.getInputStream();
        return _Bytes.of(is);
    }

    @Data
    static class Response {
        @Data
        static class Feature {
            @Data
            static class Properties {
                String lon;
                String lat;
            }
            Properties properties;
        }
        List<Feature> features;
    }

//tag::class[]
}
//end::class[]
