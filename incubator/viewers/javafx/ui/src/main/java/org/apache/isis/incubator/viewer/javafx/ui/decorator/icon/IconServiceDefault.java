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
package org.apache.isis.incubator.viewer.javafx.ui.decorator.icon;

import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.incubator.viewer.javafx.model.icon.IconService;
import org.apache.isis.viewer.common.model.decorator.icon.FontAwesomeUiModel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javafx.scene.image.Image;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Getter
@Log4j2
public class IconServiceDefault implements IconService {

    private final Map<String, Image> faIconCache = _Maps.newHashMap();
    
    @PostConstruct
    public void init() {
        log.info("about to initialize");
       // TODO preload fa icon cache 
       // fontawesome SVGs can be downloaded 
       // for SVG to Image conversion see 
       // see https://stackoverflow.com/questions/26948700/convert-svg-to-javafx-image
       // see also https://www.jensd.de/wordpress/?p=132
    }
            
    public Optional<Image> fontAwesome(String faCssClassName) {
        return Optional.ofNullable(faIconCache.get(faCssClassName));
    }

    @Override
    public Optional<Image> fontAwesome(FontAwesomeUiModel fontAwesomeUiModel) {
        return Optional.ofNullable(faIconCache.get(fontAwesomeUiModel.getCssClassesSpaceSeparated()));
    }
    
}
