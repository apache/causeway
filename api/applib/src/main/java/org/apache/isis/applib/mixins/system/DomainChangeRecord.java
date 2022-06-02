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
package org.apache.isis.applib.mixins.system;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.HasTarget;


/**
 * Allows domain objects that represents some sort of recorded change to a
 * domain object (commands, audit entries, published interactions) to act
 * as a mixee in order that other modules can contribute behaviour.
 *
 * @since 2.0 {@index}
 */
public interface DomainChangeRecord extends HasInteractionId, HasUsername, HasTarget {



    /**
     * Enumerates the different types of changes recognised.
     *
     * @since 2.0 {@index}
     */
    enum ChangeType {
        COMMAND,
        AUDIT_ENTRY,
        PUBLISHED_INTERACTION;
        @Override
        public String toString() {
            return name().replace("_", " ");
        }
    }


    @Property(
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES,
            fieldSetId="Identifiers",
            sequence = "1"
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
    @PropertyLayout(
            fieldSetId="Identifiers",
            sequence = "50"
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
     * {@link org.apache.isis.applib.services.iactn.Interaction} within which
     * this change occurred.
     */
    @Override
    @InteractionId
    UUID getInteractionId();



    @Property(
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId="Identifiers",
            sequence = "10"
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
    @PropertyLayout(
            fieldSetId="Identifiers",
            sequence = "20"
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Timestamp {
        boolean NULLABLE = HasUsername.Username.NULLABLE;
        String ALLOWS_NULL = HasUsername.Username.ALLOWS_NULL;
    }
    /**
     * The time that the change occurred.
     */
    @Timestamp
    java.sql.Timestamp getTimestamp();


    /**
     * The object type of the domain object being changed.
     */
    @Property
    @PropertyLayout(
            named="Object Type",
            fieldSetId="Target",
            sequence = "10")
    default String getTargetObjectType() {
        return getTarget().getLogicalTypeName();
    }



    @Property(
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            named="Object",
            fieldSetId="Target",
            sequence="30"
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
     * The member interaction (ie action invocation or property edit) which caused the domain object to be changed.
     *
     * <p>
     *     Populated for commands and for published events that represent action invocations or property edits.
     * </p>
     */
    @Property(
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            named="Member",
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES,
            fieldSetId="Target",
            sequence = "20"
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface TargetMember {
        int MAX_LENGTH = HasTarget.Target.MAX_LENGTH;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @TargetMember
    String getTargetMember();




    @Property(
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES,
            fieldSetId = "Detail",
            sequence = "6"
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PreValue {
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
    String getPreValue();




    @Property(
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES,
            fieldSetId = "Detail",
            sequence = "7"
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PostValue {
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
    String getPostValue();


}
