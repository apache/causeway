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
import java.util.UUID;

import javax.inject.Named;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.mixins.system.HasInteractionId;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.util.TitleBuffer;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;

import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@Named(CommandReplayResultMapping.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
@DomainObjectLayout(
        cssClassFa = "fa-solid fa-arrow-right-from-bracket",
        titleUiEvent = CommandReplayResultMapping.TitleUiEvent.class,
        iconUiEvent = CommandReplayResultMapping.IconUiEvent.class,
        cssClassUiEvent = CommandReplayResultMapping.CssClassUiEvent.class,
        layoutUiEvent = CommandReplayResultMapping.LayoutUiEvent.class
)
@NoArgsConstructor
public abstract class CommandReplayResultMapping {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandReplayResultMapping";
    public static final String SCHEMA = CausewayModuleExtCommandLogApplib.SCHEMA;
    public static final String TABLE = "CommandReplayResultMapping";

    public static class TitleUiEvent extends CausewayModuleExtCommandLogApplib.TitleUiEvent<CommandReplayResultMapping> { }
    public static class IconUiEvent extends CausewayModuleExtCommandLogApplib.IconUiEvent<CommandReplayResultMapping> { }
    public static class CssClassUiEvent extends CausewayModuleExtCommandLogApplib.CssClassUiEvent<CommandReplayResultMapping> { }
    public static class LayoutUiEvent extends CausewayModuleExtCommandLogApplib.LayoutUiEvent<CommandReplayResultMapping> { }

    @UtilityClass
    public static class Nq {
        public static final String FIND = LOGICAL_TYPE_NAME + ".find";
        public static final String FIND_CHANGED = LOGICAL_TYPE_NAME + ".findChanged";
        public static final String FIND_BY_RECORDED_BOOKMARK = LOGICAL_TYPE_NAME + ".findByRecordedBookmark";
        public static final String FIND_BY_ACTUAL_BOOKMARK = LOGICAL_TYPE_NAME + ".findByActualBookmark";
    }

    @ObjectSupport public String title() {
        return new TitleBuffer()
                .append(getRecordedBookmark())
                .append(" → ")
                .append(getActualBookmark())
                .toString();
    }

    @Programmatic
    public void init(final Bookmark recordedBookmark, final Bookmark actualBookmark) {
        init(recordedBookmark, actualBookmark, null);
    }

    @Programmatic
    public void init(
            final Bookmark recordedBookmark,
            final Bookmark actualBookmark,
            final UUID commandInteractionId) {
        setRecordedBookmark(recordedBookmark);
        setActualBookmark(actualBookmark);
        setCommandInteractionId(commandInteractionId);
    }

    @Property(
            domainEvent = RecordedBookmark.DomainEvent.class
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RecordedBookmark {
        class DomainEvent extends CausewayModuleExtCommandLogApplib.PropertyDomainEvent<CommandReplayResultMapping, Bookmark> {}
        int MAX_LENGTH = 2000;
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @RecordedBookmark
    public abstract Bookmark getRecordedBookmark();
    public abstract void setRecordedBookmark(Bookmark recordedBookmark);

    @Property(
            domainEvent = ActualBookmark.DomainEvent.class
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ActualBookmark {
        class DomainEvent extends CausewayModuleExtCommandLogApplib.PropertyDomainEvent<CommandReplayResultMapping, Bookmark> {}
        int MAX_LENGTH = 2000;
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @ActualBookmark
    public abstract Bookmark getActualBookmark();
    public abstract void setActualBookmark(Bookmark actualBookmark);

    @Property(
            domainEvent = CommandInteractionId.DomainEvent.class,
            optionality = Optionality.OPTIONAL
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CommandInteractionId {
        class DomainEvent extends CausewayModuleExtCommandLogApplib.PropertyDomainEvent<CommandReplayResultMapping, UUID> {}
        int MAX_LENGTH = HasInteractionId.InteractionId.MAX_LENGTH;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @CommandInteractionId
    public abstract UUID getCommandInteractionId();
    public abstract void setCommandInteractionId(UUID commandInteractionId);

}
