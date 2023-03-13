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
package org.apache.causeway.viewer.wicket.model.models;

import org.apache.causeway.applib.services.publishing.spi.PageRenderSubscriber;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Enumerates the different types of pages that can be rendered.
 *
 * <p>
 * Is used by <code>PagePageClassRegistry</code> to lookup the concrete page to render
 * different types of pages. This allows the large-scale structure of page
 * layout (eg headers, footers) to be altered.
 */
@RequiredArgsConstructor
public enum PageType {
    SIGN_IN(PageRenderSubscriber.PageType.OTHER),
    SIGN_UP(PageRenderSubscriber.PageType.OTHER),
    SIGN_UP_VERIFY(PageRenderSubscriber.PageType.OTHER),
    PASSWORD_RESET(PageRenderSubscriber.PageType.OTHER),
    HOME(PageRenderSubscriber.PageType.OTHER),
    HOME_AFTER_PAGETIMEOUT(PageRenderSubscriber.PageType.OTHER),
    ABOUT(PageRenderSubscriber.PageType.OTHER),
    ENTITY(PageRenderSubscriber.PageType.DOMAIN_OBJECT),
    STANDALONE_COLLECTION(PageRenderSubscriber.PageType.COLLECTION),
    VALUE(PageRenderSubscriber.PageType.VALUE),
    VOID_RETURN(PageRenderSubscriber.PageType.OTHER);

    @Getter @Accessors(fluent=true)
    private final PageRenderSubscriber.PageType asApplibPageType;

}
