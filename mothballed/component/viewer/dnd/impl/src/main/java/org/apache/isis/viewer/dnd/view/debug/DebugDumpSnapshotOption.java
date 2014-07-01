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

package org.apache.isis.viewer.dnd.view.debug;

import static org.apache.isis.core.commons.lang.ObjectExtensions.asEnumerationT;

import java.util.Enumeration;

import org.slf4j.Logger;

import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.runtime.logging.SnapshotAppender;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

/**
 * Display debug window
 */
public class DebugDumpSnapshotOption extends UserActionAbstract {
    public DebugDumpSnapshotOption() {
        super("Dump log snapshot", ActionType.DEBUG);
    }

    @Override
    public Consent disabled(final View component) {
        final Enumeration<Logger> enumeration = asEnumerationT(org.apache.log4j.Logger.getRootLogger().getAllAppenders(), Logger.class);
        while (enumeration.hasMoreElements()) {
            final org.apache.log4j.Appender appender = (org.apache.log4j.Appender) enumeration.nextElement();
            if (appender instanceof SnapshotAppender) {
                return Allow.DEFAULT;
            }
        }
        // TODO: move logic into Facet
        return new Veto("No available snapshot appender");
    }

    @Override
    public void execute(final Workspace workspace, final View view, final Location at) {
        final Enumeration<Logger> enumeration = asEnumerationT(org.apache.log4j.Logger.getRootLogger().getAllAppenders(), Logger.class);
        while (enumeration.hasMoreElements()) {
            final org.apache.log4j.Appender appender = (org.apache.log4j.Appender) enumeration.nextElement();
            if (appender instanceof SnapshotAppender) {
                ((SnapshotAppender) appender).forceSnapshot();
            }
        }
    }

    @Override
    public String getDescription(final View view) {
        return "Force a snapshot of the log";
    }
}
