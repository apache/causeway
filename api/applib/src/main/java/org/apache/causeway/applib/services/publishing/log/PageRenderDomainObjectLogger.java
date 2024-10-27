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
package org.apache.causeway.applib.services.publishing.log;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.metrics.MetricsService;
import org.apache.causeway.applib.services.publishing.spi.PageRenderSubscriber;
import org.apache.causeway.applib.services.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Simple implementation of {@link PageRenderSubscriber} that just
 * logs to a debug log.
 *
 * @since 2.1 {@index}
 */
@Service
@Named(PageRenderDomainObjectLogger.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.LATE)
@Qualifier("Logging")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class PageRenderDomainObjectLogger implements PageRenderSubscriber {

    private final MetricsService metricsService;
    private final UserService userService;

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".PageRenderDomainObjectLogger";

    @Override
    public boolean isEnabled() {
        return log.isDebugEnabled();
    }

    private static ThreadLocal<Timing> timings = ThreadLocal.withInitial(Timing::new);

    @Override
    public void onRenderingDomainObject(final Bookmark bookmark) {
        if(log.isDebugEnabled()) {
            log.debug("rendering object: [ \"{}\" ]  user: {}", bookmark.stringify(), userService.currentUserNameElseNobody());
        }
        timings.set(new Timing());
    }

    @Override
    public void onRenderedDomainObject(final Bookmark bookmark) {
        if(log.isInfoEnabled()) {
            var timing = timings.get();
            // until @ActionLayout#redirectPolicy is reintroduced (if it ever is), there's no point in querying for the numberEntitiesDirtied,
            // because (for Wicket viewer at least), the rendering is in a separate request to any modifying action.
            log.info("rendered object: [ \"{}\" ]  user: {}  took: {}ms  numEntitiesLoaded: {}", bookmark.stringify(), userService.currentUserNameElseNobody(), timing.took(), metricsService.numberEntitiesLoaded());
        }
    }

}
