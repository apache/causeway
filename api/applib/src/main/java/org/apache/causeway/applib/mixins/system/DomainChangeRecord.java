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
package org.apache.causeway.applib.mixins.system;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.mixins.security.HasUsername;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.HasTarget;


/**
 * Allows domain objects that represents some sort of recorded change to a
 * domain object (commands, executions, audit entries) to act
 * as a mixee in order that other modules can contribute behaviour.
 *
 * @since 2.0 {@index}
 */
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
public interface DomainChangeRecord extends HasInteractionId, HasUsername, HasTarget {



    /**
     * Enumerates the different types of changes recognised.
     *
     * @since 2.0 {@index}
     */
    enum ChangeType {
        COMMAND,
        AUDIT_ENTRY,
        EXECUTION;
        @Override
        public String toString() {
            return name().replace("_", " ");
        }
    }


    @Property(
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Type {
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    /**
     * Distinguishes commands from audit entries from published events/interactions (when these are shown mixed together in a (standalone) table).
     */
    @Type
    ChangeType getType();



    @Property(
            editing = Editing.DISABLED
    )
    @HasInteractionId.InteractionId
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface InteractionId {
        boolean NULLABLE = HasInteractionId.InteractionId.NULLABLE;
        String ALLOWS_NULL = HasInteractionId.InteractionId.ALLOWS_NULL;
    }
    /**
     * The unique identifier of the
     * {@link org.apache.causeway.applib.services.iactn.Interaction} within which
     * this change occurred.
     */
    @Override
    @InteractionId
    UUID getInteractionId();



    @Property(
            editing = Editing.DISABLED,
            maxLength = Username.MAX_LENGTH
    )
    @Parameter(
            maxLength = Username.MAX_LENGTH
    )
    @HasUsername.Username
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Username {
        int MAX_LENGTH = HasUsername.Username.MAX_LENGTH;
        boolean NULLABLE = HasUsername.Username.NULLABLE;
        String ALLOWS_NULL = HasUsername.Username.ALLOWS_NULL;
    }
    /**
     * The user that caused the change.
     */
    @Override
    @Username
    String getUsername();


    @Property(
            editing = Editing.DISABLED
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Timestamp {
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    /**
     * The time that the change occurred.
     */
    @Timestamp
    java.sql.Timestamp getTimestamp();


    @Property(
            editing = Editing.DISABLED,
            maxLength = TargetLogicalTypeName.MAX_LENGTH
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES
    )
    @Parameter(
            maxLength = TargetLogicalTypeName.MAX_LENGTH
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface TargetLogicalTypeName {
        int MAX_LENGTH = 255;
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }

    /**
     * The logical type name of the domain object being changed.
     */
    @TargetLogicalTypeName
    default String getTargetLogicalTypeName() {
        return getTarget().getLogicalTypeName();
    }


    @Property(
            editing = Editing.DISABLED,
            maxLength = Target.MAX_LENGTH
    )
    @Parameter(
            maxLength = Target.MAX_LENGTH
    )
    @HasTarget.Target
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Target {
        int MAX_LENGTH = HasTarget.Target.MAX_LENGTH;
        boolean NULLABLE = HasTarget.Target.NULLABLE;
        String ALLOWS_NULL = HasTarget.Target.ALLOWS_NULL;
    }
    /**
     * The {@link Bookmark} identifying the domain object that has changed.
     */
    @Target
    Bookmark getTarget();



    /**
     * The (logical) member identifier (ie action id or property id) that caused the domain object to be changed.
     *
     * <p>
     *     Populated only for commands and for executions (action invocations/property edits).
     * </p>
     */
    @Property(
            editing = Editing.DISABLED,
            optionality = Optionality.MANDATORY,
            maxLength = LogicalMemberIdentifier.MAX_LENGTH
    )
    @Parameter(
            optionality = Optionality.MANDATORY,
            maxLength = LogicalMemberIdentifier.MAX_LENGTH
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface LogicalMemberIdentifier {
        int MAX_LENGTH = 255;
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @LogicalMemberIdentifier
    String getLogicalMemberIdentifier();
    default boolean hideLogicalMemberIdentifier() {
        return getType() != ChangeType.COMMAND && getType() != ChangeType.EXECUTION;
    }


    /**
     * The property Id whose value has changed.
     *
     * <p>
     *     Populated only for audit entries.
     * </p>
     */
    @Property(
            editing = Editing.DISABLED,
            optionality = Optionality.OPTIONAL,
            maxLength = PropertyId.MAX_LENGTH
    )
    @Parameter(
            optionality = Optionality.OPTIONAL,
            maxLength = PropertyId.MAX_LENGTH
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface PropertyId {
        int MAX_LENGTH = 100;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @LogicalMemberIdentifier
    default String getPropertyId() {
        return null;
    }
    default boolean hidePropertyId() {
        return getType() != ChangeType.AUDIT_ENTRY;
    }




    @Property(
            editing = Editing.DISABLED,
            optionality = Optionality.OPTIONAL,
            maxLength = PreValue.MAX_LENGTH
    )
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES
    )
    @Parameter(
            optionality = Optionality.OPTIONAL,
            maxLength = PreValue.MAX_LENGTH
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PreValue {
        int MAX_LENGTH = 255;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    /**
     * The value of the property prior to it being changed.
     *
     * <p>
     * Populated only for audit entries.
     * </p>
     */
    @PreValue
    default String getPreValue() {
        return null;
    }
    default boolean hidePreValue() {
        return getType() != ChangeType.AUDIT_ENTRY;
    }




    @Property(
            editing = Editing.DISABLED,
            optionality = Optionality.OPTIONAL,
            maxLength = PostValue.MAX_LENGTH
    )
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES
    )
    @Parameter(
            optionality = Optionality.OPTIONAL,
            maxLength = PostValue.MAX_LENGTH
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PostValue {
        int MAX_LENGTH = 255;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    /**
     * The value of the property after it has changed.
     *
     * <p>
     * Populated only for audit entries.
     * </p>
     */
    @PostValue
    default String getPostValue() {
        return null;
    }
    default boolean hidePostValue() {
        return getType() != ChangeType.AUDIT_ENTRY;
    }

}
