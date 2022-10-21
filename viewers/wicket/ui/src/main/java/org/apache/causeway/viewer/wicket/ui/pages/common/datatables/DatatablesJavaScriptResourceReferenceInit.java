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
package org.apache.causeway.viewer.wicket.ui.pages.common.datatables;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import org.apache.causeway.core.config.CausewayConfiguration;

/**
 * Javascript (client-side) extensions and fixes.
 */
public class DatatablesJavaScriptResourceReferenceInit extends JavaScriptContentHeaderItem {

    private static final long serialVersionUID = 1L;

    private static final String ID = "dataTablesInit";

    private static DatatablesJavaScriptResourceReferenceInit instance = null;

    public static final DatatablesJavaScriptResourceReferenceInit instance(
            final CausewayConfiguration configuration) {
        if (instance == null) {
            String javascript = readResource(configuration);
            instance = new DatatablesJavaScriptResourceReferenceInit(javascript, ID);
        }
        return instance;
    }

    private DatatablesJavaScriptResourceReferenceInit(final CharSequence javaScript, final String id) {
        super(javaScript, id);
    }

    private static String readResource(final CausewayConfiguration configuration) {
        String options = configuration.getViewer().getWicket().getTable().getDecoration()
                .getDataTablesNet().getOptions().orElse("");
        return readScript().replace("$PLACEHOLDER$", options);
    }

    private static String readScript() {
        return asString(new ClassPathResource("dataTables.init.js.template",
                DatatablesJavaScriptResourceReferenceInit.class));
    }

    private static String asString(final Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            return "";
        }
    }

}
