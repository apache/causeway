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

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.publishing.spi.PageRenderSubscriber;
import org.apache.causeway.applib.services.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple implementation of {@link PageRenderSubscriber} that just
 * logs to a debug log.
 *
 * @since 2.1 {@index}
 */
@Service
@Named(PageRenderValueLogger.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.LATE)
@Qualifier("Logging")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Slf4j
public class PageRenderValueLogger implements PageRenderSubscriber {

    private final UserService userService;

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".PageRenderValueLogger";

    @Override
    public boolean isEnabled() {
        return log.isDebugEnabled();
    }

    private static ThreadLocal<Timing> timings = ThreadLocal.withInitial(Timing::new);

    @Override
    public void onRenderingValue(final Object value) {
        if(log.isDebugEnabled()) {
            log.debug("rendering value: [ \"{}\" ]  user: {}", value, userService.currentUserNameElseNobody());
        }
        timings.set(new Timing());
    }

    @Override
    public void onRenderedValue(final Object value) {
        if(log.isDebugEnabled()) {
            var timing = timings.get();
            log.debug("rendered value: [ \"{}\" ]  user: {}  took: {}ms", value, userService.currentUserNameElseNobody(), timing.took());
        }
    }
}
