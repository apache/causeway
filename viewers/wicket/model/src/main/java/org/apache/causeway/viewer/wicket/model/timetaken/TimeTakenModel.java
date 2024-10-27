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
package org.apache.causeway.viewer.wicket.model.timetaken;

import java.util.Locale;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.causeway.core.metamodel.context.MetaModelContext;

/**
 * Produces render/response timing info when in prototyping mode.
 * <p>
 * Currently used to add a 'took seconds' label to bottom of data tables.
 *
 * @since 2.0
 */
public class TimeTakenModel
implements IModel<String> {
    private static final long serialVersionUID = 1L;

    public static IModel<String> createForPrototypingElseBlank(final MetaModelContext mmc) {
        return mmc.getSystemEnvironment().isPrototyping()
            ? new TimeTakenModel()
            : Model.of("");
    }

    protected TimeTakenModel() {
    }

    @Override
    public String getObject() {

        var requestCycle = RequestCycle.get();
        if(requestCycle==null) return ""; // guard against no RequestCycle available on current Thread

        final long t0Millis = requestCycle.getStartTime();
        final long t1Millis = System.currentTimeMillis();
        final double secondsSinceRequestStart = 0.001 * (t1Millis - t0Millis);

        return String.format(Locale.US, "... took %.2f seconds", secondsSinceRequestStart);
    }

}
