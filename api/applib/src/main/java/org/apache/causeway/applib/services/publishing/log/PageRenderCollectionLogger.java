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

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.publishing.spi.PageRenderSubscriber;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.commons.internal.base._NullSafe;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Simple implementation of {@link PageRenderSubscriber} that just
 * logs the rendering of any collections to a debug log.
 *
 * @since 2.1 {@index}
 */
@Service
@Named(PageRenderCollectionLogger.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.LATE)
@Qualifier("Logging")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class PageRenderCollectionLogger implements PageRenderSubscriber {

    private final UserService userService;

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".PageRenderCollectionLogger";

    @Override
    public boolean isEnabled() {
        return log.isDebugEnabled();
    }

    private static ThreadLocal<Timing> timings = ThreadLocal.withInitial(Timing::new);

    @Override
    public void onRenderingCollection(final Supplier<List<Bookmark>> bookmarkSupplier) {
        final var bookmarksStringified = bookmarksStringified(bookmarkSupplier);
        if (log.isDebugEnabled()) {
            log.debug("rendering collection: [ {} ]  user: {}", bookmarksStringified, userService.currentUserNameElseNobody());
        }
        timings.set(new Timing());
    }

    @Override
    public void onRenderedCollection(final Supplier<List<Bookmark>> bookmarkSupplier) {
        final var bookmarksStringified = bookmarksStringified(bookmarkSupplier);

        var timing = timings.get();
        if (log.isDebugEnabled()) {
            log.debug("rendered collection: [ {} ]  user: {}  took: {}ms", bookmarksStringified, userService.currentUserNameElseNobody(), timing.took());
        }
    }

    // -- HELPER

    private static String bookmarksStringified(final Supplier<List<Bookmark>> bookmarkSupplier) {
        return _NullSafe.stream(bookmarkSupplier.get())
                .filter(Objects::nonNull)
                .map(Bookmark::stringify)
                .map(PageRenderCollectionLogger::doubleQuoted)
                .collect(Collectors.joining(", "));
    }

    private static String doubleQuoted(final @Nullable Object obj) {
        return "\"" + obj + "\"";
    }
}
