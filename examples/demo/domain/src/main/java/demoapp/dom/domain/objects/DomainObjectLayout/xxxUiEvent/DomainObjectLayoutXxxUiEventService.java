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
package demoapp.dom.domain.objects.DomainObjectLayout.xxxUiEvent;

import javax.inject.Named;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import lombok.val;
import lombok.extern.log4j.Log4j2;

//tag::class[]
@Service
@Named("demo.DomainObjectLayoutXxxUiEventService")
@Log4j2
public class DomainObjectLayoutXxxUiEventService {

    @EventListener
    void onTitleUiEvent(final DomainObjectLayoutXxxUiEventVm.TitleUiEvent titleUiEvent) {
        val in = titleUiEvent.getTitle();
        val out = "DomainObjectLayout-UiEvents";
        titleUiEvent.setTitle(out);
        log.info("titleUiEvent: {}->{}", in, out);
    }

    @EventListener
    void onIconUiEvent(final DomainObjectLayoutXxxUiEventVm.IconUiEvent iconUiEvent) {
        val in = iconUiEvent.getIconName();
        val out = "signature";
        iconUiEvent.setIconName(out);
        log.info("iconUiEvent: {}->{}", in, out);
    }

    @EventListener
    void onCssClassUiEvent(final DomainObjectLayoutXxxUiEventVm.CssClassUiEvent cssClassUiEvent) {
        val in = cssClassUiEvent.getCssClass();
        val out = "bg-dark";
        cssClassUiEvent.setCssClass(out);
        log.info("cssClassUiEvent: {}->{}", in, out);
    }

    @EventListener
    void onLayoutUiEvent(final DomainObjectLayoutXxxUiEventVm.LayoutUiEvent layoutUiEvent) {
        val in = layoutUiEvent.getLayout();
        val out = "alternative";
        layoutUiEvent.setLayout(out);
        log.info("layoutUiEvent: {}->{}", in, out);
    }

}
//end::class[]
