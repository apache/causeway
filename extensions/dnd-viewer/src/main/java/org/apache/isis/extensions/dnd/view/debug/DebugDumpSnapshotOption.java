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


package org.apache.isis.extensions.dnd.view.debug;

import static org.apache.isis.commons.lang.CastUtils.enumerationOver;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.isis.metamodel.consent.Allow;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.Veto;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.Workspace;
import org.apache.isis.extensions.dnd.view.option.UserActionAbstract;
import org.apache.isis.runtime.logging.SnapshotAppender;


/**
 * Display debug window
 */
public class DebugDumpSnapshotOption extends UserActionAbstract {
    public DebugDumpSnapshotOption() {
        super("Dump log snapshot", ObjectActionType.DEBUG);
    }

    @Override
    public Consent disabled(final View component) {
        final Enumeration<Logger> enumeration = enumerationOver(Logger.getRootLogger().getAllAppenders(), Logger.class);
        while (enumeration.hasMoreElements()) {
            final Appender appender = (Appender) enumeration.nextElement();
            if (appender instanceof SnapshotAppender) {
                return Allow.DEFAULT;
            }
        }
        // TODO: move logic into Facet
        return new Veto("No available snapshot appender");
    }

    @Override
    public void execute(final Workspace workspace, final View view, final Location at) {
        final Enumeration<Logger> enumeration = enumerationOver(Logger.getRootLogger().getAllAppenders(), Logger.class);
        while (enumeration.hasMoreElements()) {
            final Appender appender = (Appender) enumeration.nextElement();
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
