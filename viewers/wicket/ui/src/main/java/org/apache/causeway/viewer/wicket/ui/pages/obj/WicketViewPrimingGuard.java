/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.wicket.ui.pages.obj;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycle;

/** Request-local page identity admission; never stored in serialized page state. */
final class WicketViewPrimingGuard {
    private WicketViewPrimingGuard() { }
    private static final MetaDataKey<Set<Object>> PRIMED_PAGES = new MetaDataKey<>() {
        private static final long serialVersionUID = 1L;
    };

    static boolean firstVisit(final Object page) {
        var cycle = RequestCycle.get();
        if (cycle == null) return true;
        var pages = cycle.getMetaData(PRIMED_PAGES);
        if (pages == null) {
            pages = Collections.newSetFromMap(new IdentityHashMap<>());
            cycle.setMetaData(PRIMED_PAGES, pages);
        }
        return pages.add(page);
    }
}
