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
package org.apache.causeway.viewer.thymeflux.test.features;

import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ThymefluxFeaturesController {

    @GetMapping("/tflux-features")
    public String features(final Model model) {

        model.addAttribute("progress", 15);

        return "features";
    }


    @PostMapping("/tflux-features/htmx/greeting")
    public String htmxGreeting(final Model model){

        model.addAttribute("greeting", "gotcha, it is " + LocalTime.now());

        return "partial/greeting";
    }

    @PostMapping("/tflux-features/htmx/echo")
    public String htmxEcho(
            final @RequestBody String search,
            final Model model){

        model.addAttribute("echo", search);

        return "partial/echo";
    }

}
