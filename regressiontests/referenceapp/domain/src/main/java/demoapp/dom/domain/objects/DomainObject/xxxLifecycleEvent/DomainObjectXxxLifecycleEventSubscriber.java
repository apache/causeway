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
package demoapp.dom.domain.objects.DomainObject.xxxLifecycleEvent;

import java.util.LinkedList;
import java.util.List;

import jakarta.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.clock.ClockService;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
class DomainObjectXxxLifecycleEventSubscriber {

    final BookmarkService bookmarkService;
    final ClockService clockService;

    @EventListener(DomainObjectXxxLifecycleEventEntity.LifecycleEvent.class)  // <.>
    public void on(final DomainObjectXxxLifecycleEventEntity.LifecycleEvent ev) {   // <1>
        var vm = new DomainObjectLifecycleEventVm(
                clockService.getClock().nowAsLocalDateTime(),
                ev.getClass().getSimpleName(),
                bookmarkService.bookmarkFor(ev.getSource()).map(Bookmark::toString).orElse(null)
        );
        events.add(0, vm);                                              // <.>
    }

    List<DomainObjectLifecycleEventVm> events = new LinkedList<>();

}
//end::class[]
