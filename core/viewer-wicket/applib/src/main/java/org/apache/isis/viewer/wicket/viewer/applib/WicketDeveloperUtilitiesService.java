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

package org.apache.isis.viewer.wicket.viewer.applib;

import org.apache.wicket.Application;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;

public class WicketDeveloperUtilitiesService {

    /**
     * Clears the i18n cache so that localized keys can be reloaded.
     *
     * <p>
     * Have hidden this service because it seems that Wicket automatically invalidates
     * the resource cache anyway if running in development/prototype mode.
     * </p>
     */
    @Action(
            restrictTo = RestrictTo.PROTOTYPING,
            hidden = Where.EVERYWHERE,
            semantics = SemanticsOf.IDEMPOTENT
    )
    public void resetI18nCache() {
        Application.get()
                .getResourceSettings()
                .getLocalizer().clearCache();

    }
}
