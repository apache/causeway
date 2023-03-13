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

import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.annotation.Priority;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.publishing.spi.PageRenderSubscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Simple implementation of {@link PageRenderSubscriber} that just
 * logs to a debug log.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(PageRenderLogger.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.LATE)
@Qualifier("Logging")
@Log4j2
public class PageRenderLogger implements PageRenderSubscriber {

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".ObjectRenderedLogger";

    @Override
    public boolean isEnabled() {
        return log.isDebugEnabled();
    }


    @Override
    public void onRenderedDomainObject(Bookmark bookmark) {
        log.debug("rendered object: [ \"{}\" ]", bookmark.stringify());
    }

    @Override
    public void onRenderedCollection(Supplier<List<Bookmark>> bookmarkSupplier) {
        val buf = new StringBuffer();
        val first = new boolean[] {true};
        bookmarkSupplier.get().forEach(x -> {
            if(first[0]) {
                first[0] = false;
            } else {
                buf.append(", ");
            }
            buf.append("\"").append(x.stringify()).append("\"");
        });
        log.debug("rendered collection: [ {} ]", buf.toString());
    }


    @Override
    public void onRenderedValue(Object value) {
        log.debug("rendered value: [ \"{}\" ]", value.toString());
    }
}
