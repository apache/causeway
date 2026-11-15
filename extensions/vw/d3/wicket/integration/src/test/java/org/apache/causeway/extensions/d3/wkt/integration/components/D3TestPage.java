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
package org.apache.causeway.extensions.d3.wkt.integration.components;

import java.io.Serial;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebPage;

/**
 * Minimal {@link WebPage} that hosts a single D3 panel (id {@code "d3"}), used so that
 * the full page — including header contributions rendered by {@code renderHead(...)} —
 * is produced by {@link org.apache.wicket.util.tester.WicketTester}.
 */
class D3TestPage extends WebPage {

    @Serial
    private static final long serialVersionUID = 1L;

    D3TestPage(final Component component) {
        add(component);
    }
}
