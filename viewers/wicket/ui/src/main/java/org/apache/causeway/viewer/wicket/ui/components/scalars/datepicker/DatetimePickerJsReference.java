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
package org.apache.causeway.viewer.wicket.ui.components.scalars.datepicker;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import de.agilecoders.wicket.core.util.Dependencies;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

/**
 * Clone of {@link de.agilecoders.wicket.extensions.markup.html.bootstrap.references.DatetimePickerJsReference},
 * with custom moment.js dependency.
 */
public class DatetimePickerJsReference extends WebjarsJavaScriptResourceReference {

    private static final long serialVersionUID = 1L;

    /**
     * Singleton instance of this reference
     */
    private static final class Holder {

        private static final DatetimePickerJsReference INSTANCE = new DatetimePickerJsReference();
    }

    /**
     * @return the single instance of the resource reference
     */
    public static DatetimePickerJsReference instance() {
        return Holder.INSTANCE;
    }

    /**
     * Private constructor.
     */
    private DatetimePickerJsReference() {
        super("tempusdominus-bootstrap-4/current/js/tempusdominus-bootstrap-4.js");
    }

    @Override
    public List<HeaderItem> getDependencies() {
        return Dependencies.combine(super.getDependencies(),
            JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference()),
            MomentJsReference.asHeaderItem());
    }

    /**
     * @return this resource reference singleton instance as header item
     */
    public static HeaderItem asHeaderItem() {
        return JavaScriptHeaderItem.forReference(instance());
    }
}
