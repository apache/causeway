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
package org.apache.causeway.extensions.commandlog.applib.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.Priority;
import jakarta.inject.Named;

import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.mixins.system.HasInteractionId;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.applib.services.tablecol.TableColumnOrderForCollectionTypeAbstract;
import org.apache.causeway.applib.util.TitleBuffer;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;

import lombok.experimental.UtilityClass;

/**
 * Persistent association between a result bookmark recorded on the source system and the
 * bookmark returned when the command is replayed locally.
 *
 * @since 4.0 {@index}
 */
@Named(CommandReplayResultMapping.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED)
@DomainObjectLayout(
        cssClassFa = "fa-solid fa-arrow-right-from-bracket",
        titleUiEvent = CommandReplayResultMapping.TitleUiEvent.class,
        iconUiEvent = CommandReplayResultMapping.IconUiEvent.class,
        cssClassUiEvent = CommandReplayResultMapping.CssClassUiEvent.class,
        layoutUiEvent = CommandReplayResultMapping.LayoutUiEvent.class)
public interface CommandReplayResultMapping extends CommandRecordingSuppressed {

    String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandReplayResultMapping";
    String SCHEMA = CausewayModuleExtCommandLogApplib.SCHEMA;
    String TABLE = "CommandReplayResultMapping";

    class TitleUiEvent extends CausewayModuleExtCommandLogApplib.TitleUiEvent<CommandReplayResultMapping> { }
    class IconUiEvent extends CausewayModuleExtCommandLogApplib.IconUiEvent<CommandReplayResultMapping> { }
    class CssClassUiEvent extends CausewayModuleExtCommandLogApplib.CssClassUiEvent<CommandReplayResultMapping> { }
    class LayoutUiEvent extends CausewayModuleExtCommandLogApplib.LayoutUiEvent<CommandReplayResultMapping> { }

    abstract class PropertyDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.PropertyDomainEvent<CommandReplayResultMapping, T> { }

    @UtilityClass
    class Nq {
        public static final String FIND = LOGICAL_TYPE_NAME + ".find";
        public static final String FIND_CHANGED = LOGICAL_TYPE_NAME + ".findChanged";
        public static final String FIND_BY_RECORDED_BOOKMARK = LOGICAL_TYPE_NAME + ".findByRecordedBookmark";
        public static final String FIND_BY_ACTUAL_BOOKMARK = LOGICAL_TYPE_NAME + ".findByActualBookmark";
    }

    @ObjectSupport
    default String title() {
        return new TitleBuffer()
                .append(getRecordedBookmark())
                .append("→")
                .append(getActualBookmark())
                .toString();
    }

    @Programmatic
    default void init(
            final Bookmark recordedBookmark,
            final Bookmark actualBookmark,
            final @Nullable UUID commandInteractionId) {
        setRecordedBookmark(recordedBookmark);
        setActualBookmark(actualBookmark);
        setCommandInteractionId(commandInteractionId);
    }

    @Property(domainEvent = RecordedBookmark.DomainEvent.class)
    @java.lang.annotation.Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface RecordedBookmark {
        class DomainEvent extends PropertyDomainEvent<Bookmark> { }
        int MAX_LENGTH = 2000;
        boolean NULLABLE = false;
    }

    @RecordedBookmark
    Bookmark getRecordedBookmark();
    void setRecordedBookmark(Bookmark recordedBookmark);

    @Property(domainEvent = ActualBookmark.DomainEvent.class)
    @java.lang.annotation.Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface ActualBookmark {
        class DomainEvent extends PropertyDomainEvent<Bookmark> { }
        int MAX_LENGTH = 2000;
        boolean NULLABLE = false;
    }

    @ActualBookmark
    Bookmark getActualBookmark();
    void setActualBookmark(Bookmark actualBookmark);

    @Property(
            domainEvent = CommandInteractionId.DomainEvent.class,
            maxLength = CommandInteractionId.MAX_LENGTH,
            optionality = Optionality.OPTIONAL)
    @java.lang.annotation.Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface CommandInteractionId {
        class DomainEvent extends PropertyDomainEvent<UUID> { }
        int MAX_LENGTH = HasInteractionId.InteractionId.MAX_LENGTH;
        boolean NULLABLE = true;
    }

    @CommandInteractionId
    @Nullable UUID getCommandInteractionId();
    void setCommandInteractionId(@Nullable UUID commandInteractionId);

    @Service
    @Priority(PriorityPrecedence.LATE - 10)
    class TableColumnOrderDefault
            extends TableColumnOrderForCollectionTypeAbstract<CommandReplayResultMapping> {

        public TableColumnOrderDefault() {
            super(CommandReplayResultMapping.class);
        }

        @Override
        protected List<String> orderParented(
                final Object parent,
                final String collectionId,
                final List<String> propertyIds) {
            return ordered();
        }

        @Override
        protected List<String> orderStandalone(final List<String> propertyIds) {
            return ordered();
        }

        private static List<String> ordered() {
            return Arrays.asList("recordedBookmark", "actualBookmark", "commandInteractionId");
        }
    }
}
