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
package org.apache.causeway.viewer.thymeflux.model.root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.viewer.commons.applib.services.header.HeaderUiService;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Controller
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ThymefluxRootController {

    private final InteractionService interactionService;
    private final HeaderUiService headerUiModelProvider;

    @RequestMapping("/tflux")
    public String root(final Model model) {

        val svenMockup = UserMemento.ofNameAndRoleNames("sven", "causeway-ext-secman-admin", "demo");
        val interactionContextMockup = InteractionContext.ofUserWithSystemDefaults(svenMockup);

        interactionService.run(interactionContextMockup, ()->{

            var headerUiModel = headerUiModelProvider.getHeader();
            model.addAttribute("headerUiModel", headerUiModel);
        });

        //TODO on error use error template instead
        return "root";
    }

}
