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
package org.apache.causeway.viewer.restfulobjects.applib.menubars;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;

/**
 * Not part of the Restful Objects spec.
 *
 * @since 1.x {@index}
 */
@RequestMapping("${causeway.viewer.restfulobjects.base-path}/menuBars")
public interface MenuBarsResource {

    /**
     * Not part of the Restful Objects spec.
     */
    @GetMapping(path = "/", produces = {
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_JSON_LAYOUT_MENUBARS,
            MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_LAYOUT_MENUBARS
    })
    ResponseEntity<Object> menuBars();

    /**
     * Not part of the Restful Objects spec.
     */
    @DeleteMapping
    ResponseEntity<Object> deleteMenuBarsNotAllowed();

    /**
     * Not part of the Restful Objects spec.
     */
    @PutMapping
    ResponseEntity<Object> putMenuBarsNotAllowed();

    /**
     * Not part of the Restful Objects spec.
     */
    @PostMapping
    ResponseEntity<Object> postMenuBarsNotAllowed();

}
