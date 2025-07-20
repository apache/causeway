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
 *
 */
package org.apache.causeway.viewer.wicket.ui.pages.common.datatables;

import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;

import lombok.Getter;
import lombok.experimental.Accessors;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

public class DatatablesJavaScriptReferenceWkt extends WebjarsJavaScriptResourceReference {
    private static final long serialVersionUID = 1L;

    @Getter(lazy = true) @Accessors(fluent = true)
    private static final DatatablesJavaScriptReferenceWkt instance =
        new DatatablesJavaScriptReferenceWkt();

    public static JavaScriptReferenceHeaderItem asHeaderItem() {
        return JavaScriptHeaderItem.forReference(DatatablesJavaScriptReferenceWkt.instance());
    }

    /**
     * Private constructor.
     */
    private DatatablesJavaScriptReferenceWkt() {
        super(RESOURCE);
    }

    private static final String RESOURCE = DatatablesDotNet.formatWithVersion(
            "datatables/%s/js/dataTables.min.js");
}
