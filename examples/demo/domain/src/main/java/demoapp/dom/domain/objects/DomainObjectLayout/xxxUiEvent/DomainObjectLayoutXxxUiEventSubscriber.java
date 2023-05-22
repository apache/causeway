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

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.title.TitleService;

import lombok.val;
import lombok.extern.log4j.Log4j2;

//tag::class[]
@Service
@Named("demo.DomainObjectLayoutXxxUiEventService")
@Log4j2
public class DomainObjectLayoutXxxUiEventSubscriber {
    // ...

//end::class[]

//tag::titleUiEvent[]
    @EventListener
    void onTitleUiEvent(final DomainObjectLayoutXxxUiEventEntity.TitleEvent titleUiEvent) {
        val source = titleUiEvent.getSource();
        titleUiEvent.setTitle(
                hasNameInFirstHalfOfAlphabet(source)
                        ? source.getName().toUpperCase()
                        : source.getName().toLowerCase());
    }
//end::titleUiEvent[]

//tag::iconUiEvent[]
    @EventListener
    void onIconUiEvent(final DomainObjectLayoutXxxUiEventEntity.IconEvent iconUiEvent) {
        val source = iconUiEvent.getSource();
        if (hasNameInFirstHalfOfAlphabet(source)) {
            iconUiEvent.setIconName("signature");
        }
    }
//end::iconUiEvent[]

//tag::cssClassUiEvent[]
    @EventListener
    void onCssClassUiEvent(final DomainObjectLayoutXxxUiEventEntity.CssClassEvent cssClassUiEvent) {
        val source = cssClassUiEvent.getSource();
        cssClassUiEvent.setCssClass(hasNameInFirstHalfOfAlphabet(source)
                ? "custom1"
                : "custom2");
    }
//end::cssClassUiEvent[]

//tag::layoutUiEvent[]
    @EventListener
    void onLayoutUiEvent(final DomainObjectLayoutXxxUiEventEntity.LayoutEvent layoutUiEvent) {
        val source = layoutUiEvent.getSource();
        layoutUiEvent.setLayout(
                hasNameInFirstHalfOfAlphabet(source)
                    ? "alternative1"
                    : "alternative2");
    }
//end::layoutUiEvent[]

//tag::class[]
    private boolean hasNameInFirstHalfOfAlphabet(DomainObjectLayoutXxxUiEventEntity source) {
        return source.getName().toLowerCase().compareTo("m") < 0;
    }

    @Inject TitleService titleService;
}
//end::class[]
