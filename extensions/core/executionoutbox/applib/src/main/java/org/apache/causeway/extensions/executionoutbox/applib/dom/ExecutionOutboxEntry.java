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
package org.apache.causeway.extensions.executionoutbox.applib.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.Digits;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.mixins.system.DomainChangeRecord;
import org.apache.causeway.applib.mixins.system.HasInteractionId;
import org.apache.causeway.applib.mixins.system.HasInteractionIdAndSequence;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.iactn.ActionInvocation;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.HasInteractionDto;
import org.apache.causeway.applib.services.iactn.PropertyEdit;
import org.apache.causeway.applib.services.tablecol.TableColumnOrderForCollectionTypeAbstract;
import org.apache.causeway.applib.util.ObjectContracts;
import org.apache.causeway.applib.util.TitleBuffer;
import org.apache.causeway.applib.util.ToString;
import org.apache.causeway.commons.internal.base._Temporals;
import org.apache.causeway.extensions.executionoutbox.applib.CausewayModuleExtExecutionOutboxApplib;
import org.apache.causeway.schema.ixn.v2.InteractionDto;
import org.apache.causeway.schema.ixn.v2.MemberExecutionDto;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * A persistent representation of an {@link Execution execution} (property edit or
 * action invocation), within a wider {@link org.apache.causeway.applib.services.iactn.Interaction interaction}.
 *
 *  Note that this class implements {@link HasInteractionDto}, providing access to
 *  {@link org.apache.causeway.applib.services.iactn.Interaction}, a serialized representation containing the {@link Execution}.
 *
 * @since 2.0 {@index}
 */
@Named(ExecutionOutboxEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
@DomainObjectLayout(
        titleUiEvent = ExecutionOutboxEntry.TitleUiEvent.class,
        iconUiEvent = ExecutionOutboxEntry.IconUiEvent.class,
        cssClassUiEvent = ExecutionOutboxEntry.CssClassUiEvent.class,
        layoutUiEvent = ExecutionOutboxEntry.LayoutUiEvent.class
)
@NoArgsConstructor
public abstract class ExecutionOutboxEntry
implements Comparable<ExecutionOutboxEntry>, DomainChangeRecord, HasInteractionIdAndSequence, HasInteractionDto {

    public final static String LOGICAL_TYPE_NAME = CausewayModuleExtExecutionOutboxApplib.NAMESPACE + ".ExecutionOutboxEntry";
    public static final String SCHEMA = CausewayModuleExtExecutionOutboxApplib.SCHEMA;
    public static final String TABLE = "ExecutionOutboxEntry";

    public static class TitleUiEvent extends CausewayModuleExtExecutionOutboxApplib.TitleUiEvent<ExecutionOutboxEntry> { }
    public static class IconUiEvent extends CausewayModuleExtExecutionOutboxApplib.IconUiEvent<ExecutionOutboxEntry> { }
    public static class CssClassUiEvent extends CausewayModuleExtExecutionOutboxApplib.CssClassUiEvent<ExecutionOutboxEntry> { }
    public static class LayoutUiEvent extends CausewayModuleExtExecutionOutboxApplib.LayoutUiEvent<ExecutionOutboxEntry> { }

    public static abstract class PropertyDomainEvent<T> extends CausewayModuleExtExecutionOutboxApplib.PropertyDomainEvent<ExecutionOutboxEntry, T> { }
    public static abstract class CollectionDomainEvent<T> extends CausewayModuleExtExecutionOutboxApplib.CollectionDomainEvent<ExecutionOutboxEntry, T> { }
    public static abstract class ActionDomainEvent extends CausewayModuleExtExecutionOutboxApplib.ActionDomainEvent<ExecutionOutboxEntry> { }

    @UtilityClass
    public static class Nq {
        public static final String FIND_BY_INTERACTION_ID_AND_SEQUENCE = LOGICAL_TYPE_NAME + ".findByInteractionIdAndSequence";
        public static final String FIND_OLDEST = LOGICAL_TYPE_NAME + ".findOldest";
    }

    @UtilityClass
    protected static class Util {
        public static String abbreviated(final String str, final int maxLength) {
            return str != null ? (str.length() < maxLength ? str : str.substring(0, maxLength - 3) + "...") : null;
        }
    }

    @Inject BookmarkService bookmarkService;

    public ExecutionOutboxEntry(@NonNull final Execution<? extends MemberExecutionDto,?> execution) {
        init(execution);
    }

    @Programmatic
    public void init(final Execution<? extends MemberExecutionDto, ?> execution) {
        val interactionId = execution.getInteraction().getInteractionId();
        setInteractionId(interactionId);

        val memberExecutionDto = execution.getDto();
        setSequence(memberExecutionDto.getSequence());

        val interactionDto = new InteractionDto();
        interactionDto.setInteractionId(interactionId.toString());
        interactionDto.setExecution(memberExecutionDto);
        setInteractionDto(interactionDto);

        setTimestamp(execution.getStartedAt());
        setStartedAt(execution.getStartedAt());
        setCompletedAt(execution.getCompletedAt());

        setLogicalMemberIdentifier(memberExecutionDto.getLogicalMemberIdentifier());

        setTarget(Bookmark.forOidDto(memberExecutionDto.getTarget()));
        setUsername(memberExecutionDto.getUsername());

        if(execution instanceof PropertyEdit) {
            setExecutionType(ExecutionOutboxEntryType.PROPERTY_EDIT);
        } else if(execution instanceof ActionInvocation) {
            setExecutionType(ExecutionOutboxEntryType.ACTION_INVOCATION);
        } else {
            // shouldn't happen, there are no other subtypes
            throw new IllegalArgumentException(String.format("Execution subtype unknown: %s", execution.getClass().getName()));
        }
    }


    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @ObjectSupport public String title() {
        return new TitleBuffer()
                .append(formatter.format(getTimestamp().toLocalDateTime()))
                .append(" ")
                .append(getLogicalMemberIdentifier())
                .toString();
    }


    @DomainChangeRecord.Type
    @Override
    public ChangeType getType() {
        return ChangeType.EXECUTION;
    }


    /**
     * The unique identifier (a GUID) of the {@link org.apache.causeway.applib.services.iactn.Interaction} in which this execution occurred.
     *
     * <p>
     * The combination of ({@link #getInteractionId() interactionId}, {@link #getSequence() sequence}) makes up the
     * primary key.
     * </p>
     */
    @Property(
            domainEvent = InteractionId.DomainEvent.class
    )
    @DomainChangeRecord.InteractionId
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface InteractionId {
        class DomainEvent extends PropertyDomainEvent<UUID> {}
        int MAX_LENGTH = HasInteractionId.InteractionId.MAX_LENGTH;
        String NAME = "interactionId";
        boolean NULLABLE = HasInteractionId.InteractionId.NULLABLE;
        String ALLOWS_NULL = HasInteractionId.InteractionId.ALLOWS_NULL;
    }
    @Override
    @InteractionId
    public abstract UUID getInteractionId();
    public abstract void setInteractionId(UUID interactionId);



    /**
     * The 0-based additional identifier of an execution event within the given {@link #getInteractionId() interaction}.
     *
     * <p>
     * The combination of ({@link #getInteractionId() interactionId}, {@link #getSequence() sequence}) makes up the
     * primary key.
     * </p>
     */
    @Property(
            domainEvent = Sequence.DomainEvent.class
    )
    @HasInteractionIdAndSequence.Sequence
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Sequence {
        class DomainEvent extends PropertyDomainEvent<Integer> {}
        String NAME = "sequence";
        boolean NULLABLE = HasInteractionIdAndSequence.Sequence.NULLABLE;
        String ALLOWS_NULL = HasInteractionIdAndSequence.Sequence.ALLOWS_NULL;
    }
    @Sequence
    @Override
    public abstract int getSequence();
    public abstract void setSequence(int sequence);



    @Property(
            domainEvent = ExecutionType.DomainEvent.class
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ExecutionType {
        class DomainEvent extends PropertyDomainEvent<ExecutionOutboxEntryType> {}
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
        int MAX_LENGTH = 30;
    }
    @ExecutionType
    public abstract ExecutionOutboxEntryType getExecutionType();
    public abstract void setExecutionType(ExecutionOutboxEntryType executionType);



    @Property(
            domainEvent = Username.DomainEvent.class
    )
    @DomainChangeRecord.Username
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Username {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = DomainChangeRecord.Username.MAX_LENGTH;
        boolean NULLABLE = DomainChangeRecord.Username.NULLABLE;
        String ALLOWS_NULL = DomainChangeRecord.Username.ALLOWS_NULL;
    }
    @Override
    @Username
    public abstract String getUsername();
    public abstract void setUsername(String userName);



    @Property(
            domainEvent = Timestamp.DomainEvent.class
    )
    @DomainChangeRecord.Timestamp
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Timestamp {
        class DomainEvent extends PropertyDomainEvent<java.sql.Timestamp> {}
        boolean NULLABLE = DomainChangeRecord.Timestamp.NULLABLE;
        String ALLOWS_NULL = DomainChangeRecord.Timestamp.ALLOWS_NULL;
    }
    @Timestamp
    @Override
    public abstract java.sql.Timestamp getTimestamp();
    public abstract void setTimestamp(java.sql.Timestamp timestamp);



    @Property(
            domainEvent = Target.DomainEvent.class
    )
    @DomainChangeRecord.Target
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Target {
        class DomainEvent extends PropertyDomainEvent<Bookmark> {}
        int MAX_LENGTH = DomainChangeRecord.Target.MAX_LENGTH;
        boolean NULLABLE = DomainChangeRecord.Target.NULLABLE;
        String ALLOWS_NULL = DomainChangeRecord.Target.ALLOWS_NULL;
    }
    @Override
    @Target
    public abstract Bookmark getTarget();
    public abstract void setTarget(Bookmark target);



    /**
     * String representation of the invoked action or edited property.
     *
     * <p>
     * This is the <i>logical</i> member identifier because it does not matter whether the action/property is declared
     * on the type or is contributed.
     *
     */
    @Property(
            domainEvent = LogicalMemberIdentifier.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LogicalMemberIdentifier {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = 255;
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @Override
    @LogicalMemberIdentifier
    public abstract String getLogicalMemberIdentifier();
    public abstract void setLogicalMemberIdentifier(String logicalMemberIdentifier);



    @Property(
            domainEvent = InteractionDtoAnnot.DomainEvent.class
    )
    @HasInteractionDto.InteractionDtoAnnot
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface InteractionDtoAnnot {
        class DomainEvent extends PropertyDomainEvent<InteractionDto> {}
        boolean NULLABLE = HasInteractionDto.InteractionDtoAnnot.NULLABLE;
        String ALLOWS_NULL = HasInteractionDto.InteractionDtoAnnot.ALLOWS_NULL;
    }
    @InteractionDtoAnnot
    @Override
    public abstract InteractionDto getInteractionDto();
    public abstract void setInteractionDto(InteractionDto commandDto);



    @Property(
            domainEvent = StartedAt.DomainEvent.class,
            editing = Editing.DISABLED,
            optionality = Optionality.OPTIONAL
    )
    @Parameter(
            optionality = Optionality.OPTIONAL
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StartedAt {
        class DomainEvent extends PropertyDomainEvent<java.sql.Timestamp> {}
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @StartedAt
    public abstract java.sql.Timestamp getStartedAt();
    public abstract void setStartedAt(java.sql.Timestamp startedAt);



    @Property(
            domainEvent = CompletedAt.DomainEvent.class,
            editing = Editing.DISABLED,
            optionality = Optionality.OPTIONAL
    )
    @Parameter(
            optionality = Optionality.OPTIONAL
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CompletedAt {
        class DomainEvent extends PropertyDomainEvent<java.sql.Timestamp> {}
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @CompletedAt
    public abstract java.sql.Timestamp getCompletedAt();
    public abstract void setCompletedAt(java.sql.Timestamp completedAt);



    @Property(
            domainEvent = Duration.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @Digits(integer=Duration.DIGITS_INTEGER, fraction=Duration.DIGITS_FRACTION)
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Duration {
        class DomainEvent extends PropertyDomainEvent<BigDecimal> {}
        int DIGITS_INTEGER = 5;
        int DIGITS_FRACTION = 3;
    }
    /**
     * The number of seconds (to 3 decimal places) that this execution lasted, derived from
     * {@link #getStartedAt()} and {@link #getCompletedAt()}.
     */
    @Duration
    public BigDecimal getDuration() {
        return _Temporals.secondsBetweenAsDecimal(getStartedAt(), getCompletedAt())
                .orElse(null);
    }


    @Override
    public int compareTo(final ExecutionOutboxEntry other) {
        return this.getTimestamp().compareTo(other.getTimestamp());
    }


    static final ToString<ExecutionOutboxEntry> stringifier = ObjectContracts
            .toString("interactionId", ExecutionOutboxEntry::getInteractionId)
            .thenToString("sequence", ExecutionOutboxEntry::getSequence)
            .thenToString("username", ExecutionOutboxEntry::getUsername)
            .thenToString("type", ExecutionOutboxEntry::getExecutionType)
            .thenToString("timestamp", ExecutionOutboxEntry::getTimestamp)
            .thenToString("target", ExecutionOutboxEntry::getTarget)
            .thenToString("logicalMemberIdentifier", ExecutionOutboxEntry::getLogicalMemberIdentifier)
            .thenToStringOmitIfAbsent("startedAt", ExecutionOutboxEntry::getStartedAt)
            .thenToStringOmitIfAbsent("completedAt", ExecutionOutboxEntry::getCompletedAt);

    @Override
    public String toString() {
        return stringifier.toString(this);
    }

    @Service
    @Priority(PriorityPrecedence.LATE - 10) // before the framework's own default.
    public static class TableColumnOrderDefault extends TableColumnOrderForCollectionTypeAbstract<ExecutionOutboxEntry> {

        public TableColumnOrderDefault() { super(ExecutionOutboxEntry.class); }

        @Override
        protected List<String> orderParented(final Object parent, final String collectionId, final List<String> propertyIds) {
            return ordered(propertyIds);
        }

        @Override
        protected List<String> orderStandalone(final List<String> propertyIds) {
            return ordered(propertyIds);
        }

        private List<String> ordered(final List<String> propertyIds) {
            return Arrays.asList(
                    "timestamp", "target", "logicalMemberIdentifier", "username", "duration", "interactionId", "sequence"
            );
        }
    }

}
