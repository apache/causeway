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
package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import static org.apache.causeway.extensions.commandlog.applib.dom.replay.TimestampMarshallUtil.fromString;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import lombok.Getter;

@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout(cssClassFa = "solid share-from-square")
@Named(CommandExportManager.LOGICAL_TYPE_NAME)
public final class CommandExportManager implements ViewModel, HasBaseline {

	public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandExportManager";

    public static abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }

    ReplayContext replayContext;

    @Inject
    public CommandExportManager(
            final String memento,
            final ReplayContext replayContext) {
        this(fromString(memento, replayContext.clockService().getClock().nowAsJavaSqlTimestamp()),  replayContext);
    }

    public CommandExportManager(
            final java.sql.Timestamp baseline,
            final ReplayContext replayContext) {
        this.baseline = baseline;
        this.replayContext = replayContext;
    }

    @ObjectSupport public String title() {
        return "Command Export Manager";
    }


    @Property
    @PropertyLayout(describedAs = "Only commands after this timestamp are available for export")
    @Getter
    private java.sql.Timestamp baseline;


    @Override
    @Programmatic
    public CommandExportManager withBaseline(Timestamp baseline) {
        return new CommandExportManager(baseline, replayContext);
    }


    // -- NOT YET EXPORTED

    @Collection
    @CollectionLayout(
            describedAs = "Commands that can be exported"
    )
    public List<ReplayableCommand> getNotYetExported() {
        return commandLogEntryRepository().findForegroundSinceTimestampAndCanBeExported(baseline).stream()
            .map(entry->new ReplayableCommand(
                    entry.getInteractionId(),
                    replayContext))
            .collect(Collectors.toList());
    }


    // -- EXPORTED

    @Collection
    @CollectionLayout(describedAs = "Commands that have been exported")
    public List<ReplayableCommand> getExported() {
        return commandLogEntryRepository().findForegroundSinceTimestampAndHasBeenExported(baseline).stream()
            .map(entry->new ReplayableCommand(
                    entry.getInteractionId(),
                    replayContext))
            .collect(Collectors.toList());
    }


    // -- VM STATE

    @Override
    public String viewModelMemento() {
        return TimestampMarshallUtil.toString(this.baseline);
    }

    // -- HELPER
    private CommandLogEntryRepository commandLogEntryRepository() {
        return replayContext.commandLogEntryRepository();
    }
}
