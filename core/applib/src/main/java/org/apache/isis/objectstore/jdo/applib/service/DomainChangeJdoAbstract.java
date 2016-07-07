/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.objectstore.jdo.applib.service;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.metamodel.MetaModelService2;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.applib.services.publish.PublishingService;
import org.apache.isis.applib.util.ObjectContracts;

import static org.apache.isis.applib.annotation.Optionality.MANDATORY;
import static org.apache.isis.applib.annotation.Optionality.OPTIONAL;

/**
 * An abstraction of some sort of recorded change to a domain object, either a <tt>CommandJdo</tt>, an
 * <tt>AuditEntryJdo</tt> or a <tt>PublishedEventJdo</tt>.
 */
@MemberGroupLayout(
        columnSpans={6,0,6,12}, 
        left={"Identifiers"},
        right={"Target","Detail"})
@DomainObjectLayout(
        named="Domain Change"
)
@DomainObject(
        editing = Editing.DISABLED
)
public abstract class DomainChangeJdoAbstract {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(DomainChangeJdoAbstract.class);

    public static enum ChangeType {
        COMMAND,
        AUDIT_ENTRY,
        /**
         * As per {@link PublishingService}.
         *
         * @deprecated - replaced by {@link #PUBLISHED_INTERACTION} (because {@link PublishingService} has been replaced by {@link PublisherService}).
         */
        @Deprecated
        PUBLISHED_EVENT,
        /**
         * As per {@link PublisherService}.
         */
        PUBLISHED_INTERACTION;
        @Override
        public String toString() {
            return name().replace("_", " ");
        }
    }
    public DomainChangeJdoAbstract(final ChangeType changeType) {
        this.type = changeType;
    }
    
    private final ChangeType type;
    /**
     * Distinguishes commands from audit entries from published events/interactions (when these are shown mixed together in a (standalone) table).
     */
    @Property
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES
    )
    @MemberOrder(name="Identifiers", sequence = "1")
    public ChangeType getType() {
        return type;
    }


    // //////////////////////////////////////

    /**
     * The user that caused the change.
     * 
     * <p>
     * This dummy implementation is a trick so that Isis will render the property in a standalone table.  Each of the
     * subclasses override with the &quot;real&quot; implementation.
     */
    @Property
    @MemberOrder(name="Identifiers", sequence = "10")
    public String getUser() {
        return null;
    }

    // //////////////////////////////////////

    
    /**
     * The time that the change occurred.
     * 
     * <p>
     * This dummy implementation is a trick so that Isis will render the property in a standalone table.  Each of the
     * subclasses override with the &quot;real&quot; implementation.
     */
    @Property
    @MemberOrder(name="Identifiers", sequence = "20")
    public Timestamp getTimestamp() {
        return null;
    }
    
    // //////////////////////////////////////

    /**
     * The unique identifier (a GUID) of the transaction in which this change occurred.
     * 
     * <p>
     * This dummy implementation is a trick so that Isis will render the property in a standalone table.  Each of the
     * subclasses override with the &quot;real&quot; implementation.
     */
    @Property
    @MemberOrder(name="Identifiers",sequence = "50")
    public UUID getTransactionId() {
        return null;
    }

    // //////////////////////////////////////

    /**
     * The class of the domain object being changed.
     * 
     * <p>
     * This dummy implementation is a trick so that Isis will render the property in a standalone table.  Each of the
     * subclasses override with the &quot;real&quot; implementation.
     */
    @Property
    @PropertyLayout(named="Class")
    @MemberOrder(name="Target", sequence = "10")
    public String getTargetClass() {
        return null;
    }

    // //////////////////////////////////////

    @Programmatic
    public Bookmark getTarget() {
        final String str = getTargetStr();
        return str != null? new Bookmark(str): null;
    }
    
    @Programmatic
    public void setTarget(Bookmark target) {
        final String targetStr = target != null ? target.toString() : null;
        setTargetStr(targetStr);
    }

    // //////////////////////////////////////
    
    private String targetAction;
    
    /**
     * The member interaction (ie action invocation or property edit) which caused the domain object to be changed.
     *
     * <p>
     *     Populated for commands and for published events that represent action invocations or property edits.
     * </p>
     *
     * <p>
     * This dummy implementation is a trick so that Isis will render the property in a standalone table.  Each of the
     * subclasses override with the &quot;real&quot; implementation.
     * </p>
     *
     * <p>
     *     NB: commands and published events applied only to actions, hence the name of this field.  In a future release
     *     the name of this field may change to &quot;TargetMember&quot;.  Note that the {@link PropertyLayout} already uses
     *     &quot;Member&quot; this as a name hint.
     * </p>
     *
     */
    @Property(
            optionality = OPTIONAL
    )
    @PropertyLayout(
            named="Member",
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES
    )
    @MemberOrder(name="Target", sequence = "20")
    public String getTargetAction() {
        return targetAction;
    }


    // //////////////////////////////////////

    /**
     * The (string representation of the) {@link Bookmark} identifying the domain object that has changed.
     * 
     * <p>
     * This dummy implementation is a trick so that Isis will render the property in a standalone table.  Each of the
     * subclasses override with the &quot;real&quot; implementation.
     */
    @Property
    @PropertyLayout(named="Object")
    @MemberOrder(name="Target", sequence="30")
    public String getTargetStr() {
        return null;
    }
    
    /**
     * For {@link #setTarget(Bookmark)} to delegate to.
     */
    public abstract void setTargetStr(final String targetStr);


    // //////////////////////////////////////

    @Action(
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            named = "Open"
    )
    @MemberOrder(name="TargetStr", sequence="1")
    public Object openTargetObject() {
        try {
            return bookmarkService != null
                    ? bookmarkService.lookup(getTarget(), BookmarkService2.FieldResetPolicy.DONT_RESET)
                    : null;
        } catch(RuntimeException ex) {
            if(ex.getClass().getName().contains("ObjectNotFoundException")) {
                messageService.warnUser("Object not found - has it since been deleted?");
                return null;
            }
            throw ex;
        }
    }

    public boolean hideOpenTargetObject() {
        return getTarget() == null;
    }

    public String disableOpenTargetObject() {
        final Object targetObject = getTarget();
        if (targetObject == null) {
            return null;
        }
        final MetaModelService2.Sort sortOfObject = metaModelService.sortOf(getTarget());
        return !(sortOfObject.isViewModel() || sortOfObject.isJdoEntity())
                ? "Can only open view models or entities"
                : null;
    }



    // //////////////////////////////////////
    
    /**
     * The property of the object that was changed.
     * 
     * <p>
     * Populated only for <tt>AuditEntryJdo</tt>.
     * 
     * <p>
     * This dummy implementation is a trick so that Isis will render the property in a standalone table.  Each of the
     * subclasses override with the &quot;real&quot; implementation.
     */
    @Property(
            optionality = OPTIONAL
    )
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES
    )
    @MemberOrder(name="Target",sequence = "21")
    public String getPropertyId() {
        return null;
    }

    // //////////////////////////////////////


    /**
     * The value of the property prior to it being changed.
     * 
     * <p>
     * Populated only for <tt>AuditEntryJdo</tt>.
     * 
     * <p>
     * This dummy implementation is a trick so that Isis will render the property in a standalone table.  Each of the
     * subclasses override with the &quot;real&quot; implementation.
     */
    @Property(
            optionality = OPTIONAL
    )
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES
    )
    @MemberOrder(name="Detail",sequence = "6")
    public String getPreValue() {
        return null;
    }

    
    // //////////////////////////////////////

    /**
     * The value of the property after it has changed.
     * 
     * <p>
     * Populated only for <tt>AuditEntryJdo</tt>.
     * 
     * <p>
     * This dummy implementation is a trick so that Isis will render the property in a standalone table.  Each of the
     * subclasses override with the &quot;real&quot; implementation.
     */
    @Property(
            optionality = MANDATORY
    )
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES
    )
    @MemberOrder(name="Detail",sequence = "7")
    public String getPostValue() {
        return null;
    }
    

    // //////////////////////////////////////

    public static Comparator<DomainChangeJdoAbstract> compareByTimestampDescThenType(){
        return ObjectContracts.compareBy("timestamp desc","type");
    }

    public static Comparator<DomainChangeJdoAbstract> compareByTargetThenTimestampDescThenType(){
        return ObjectContracts.compareBy("targetStr","timestamp desc","type");
    }
    
    public static Comparator<DomainChangeJdoAbstract> compareByTargetThenUserThenTimestampDescThenType(){
        return ObjectContracts.compareBy("targetStr","user","timestamp desc","type");
    }
    
    public static Comparator<DomainChangeJdoAbstract> compareByUserThenTimestampDescThenType(){
        return ObjectContracts.compareBy("user","timestamp desc","type");
    }
    
    public static Comparator<DomainChangeJdoAbstract> compareByUserThenTargetThenTimestampDescThenType(){
        return ObjectContracts.compareBy("user","targetStr","timestamp desc","type");
    }

    // //////////////////////////////////////
    // dependencies
    // //////////////////////////////////////
    
    @javax.inject.Inject
    protected BookmarkService2 bookmarkService;
    
    @javax.inject.Inject
    protected MessageService messageService;

    @javax.inject.Inject
    protected MetaModelService2 metaModelService;

}
