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
package dom.todo;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.jdo.JDOHelper;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.VersionStrategy;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import org.joda.time.LocalDate;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.InvokeOn;
import org.apache.isis.applib.annotation.InvokedOn;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.actinvoc.ActionInvocationContext;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.scratchpad.Scratchpad;
import org.apache.isis.applib.services.wrapper.HiddenException;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

@javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.DATASTORE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY,
         column="id")
@javax.jdo.annotations.Version(
        strategy=VersionStrategy.VERSION_NUMBER, 
        column="version")
@javax.jdo.annotations.Uniques({
    @javax.jdo.annotations.Unique(
            name="ToDoItem_description_must_be_unique", 
            members={"ownedBy","description"})
})
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name = "findByOwnedBy", language = "JDOQL",
            value = "SELECT "
                    + "FROM dom.todo.ToDoItem "
                    + "WHERE ownedBy == :ownedBy"),
    @javax.jdo.annotations.Query(
            name = "findByOwnedByAndCompleteIsFalse", language = "JDOQL",
            value = "SELECT "
                    + "FROM dom.todo.ToDoItem "
                    + "WHERE ownedBy == :ownedBy "
                    + "   && complete == false"),
    @javax.jdo.annotations.Query(
            name = "findByOwnedByAndCompleteIsTrue", language = "JDOQL",
            value = "SELECT "
                    + "FROM dom.todo.ToDoItem "
                    + "WHERE ownedBy == :ownedBy "
                    + "&& complete == true"),
    @javax.jdo.annotations.Query(
            name = "findByOwnedByAndCategory", language = "JDOQL",
            value = "SELECT "
                    + "FROM dom.todo.ToDoItem "
                    + "WHERE ownedBy == :ownedBy "
                    + "&& category == :category"),
    @javax.jdo.annotations.Query(
            name = "findByOwnedByAndDescriptionContains", language = "JDOQL",
            value = "SELECT "
                    + "FROM dom.todo.ToDoItem "
                    + "WHERE ownedBy == :ownedBy && "
                    + "description.indexOf(:description) >= 0")
})
@DomainObject(
        autoCompleteRepository = ToDoItems.class, // for drop-downs, unless autoCompleteNXxx() is present
        autoCompleteAction = "autoComplete",
        // bounded = true,  // for drop-downs if only a small number of instances only (overrides autoComplete)
        objectType = "TODO"
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
)
public class ToDoItem implements Categorized, Comparable<ToDoItem> {

    //region > LOG
    /**
     * It isn't common for entities to log, but they can if required.  
     * Isis uses slf4j API internally (with log4j as implementation), and is the recommended API to use. 
     */
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ToDoItem.class);
    //endregion

    // region > title, icon
    public String title() {
        final TitleBuffer buf = new TitleBuffer();
        buf.append(getDescription());
        if (isComplete()) {
            buf.append("- Completed!");
        } else {
            try {
                final LocalDate dueBy = wrapperFactory.wrap(this).getDueBy();
                if (dueBy != null) {
                    buf.append(" due by", dueBy);
                }
            } catch(final HiddenException ignored) {
            }
        }
        return buf.toString();
    }
    
    public String iconName() {
        return !isComplete() ? "todo" : "done";
    }

    public String cssClass() { return iconName(); }

    //endregion

    //region > description (property)
    private String description;

    @javax.jdo.annotations.Column(allowsNull="false", length=100)
    @Property(
        regexPattern = "\\w[@&:\\-\\,\\.\\+ \\w]*"
    )
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
    public void modifyDescription(final String description) {
        setDescription(description);
    }
    public void clearDescription() {
        setDescription(null);
    }
    //endregion

    //region > dueBy (property)
    @javax.jdo.annotations.Persistent(defaultFetchGroup="true")
    private LocalDate dueBy;

    @javax.jdo.annotations.Column(allowsNull="true")
    public LocalDate getDueBy() {
        return dueBy;
    }

    /**
     * Demonstrates how to perform security checks within the domain code.
     *
     * <p>
     *     Generally speaking this approach is not recommended; such checks should
     *     wherever possible be externalized in the security subsystem.
     * </p>
     */
    public boolean hideDueBy() {
        final UserMemento user = container.getUser();
        return user.hasRole("realm1:noDueBy_role");
    }

    public void setDueBy(final LocalDate dueBy) {
        this.dueBy = dueBy;
    }
    public void clearDueBy() {
        setDueBy(null);
    }
    // proposed new value is validated before setting
    public String validateDueBy(final LocalDate dueBy) {
        if (dueBy == null) {
            return null;
        }
        return toDoItems.validateDueBy(dueBy);
    }
    //endregion

    //region > category and subcategory (property)

    public static enum Category {
        Professional {
            @Override
            public List<Subcategory> subcategories() {
                return Arrays.asList(null, Subcategory.OpenSource, Subcategory.Consulting, Subcategory.Education, Subcategory.Marketing);
            }
        }, Domestic {
            @Override
            public List<Subcategory> subcategories() {
                return Arrays.asList(null, Subcategory.Shopping, Subcategory.Housework, Subcategory.Garden, Subcategory.Chores);
            }
        }, Other {
            @Override
            public List<Subcategory> subcategories() {
                return Arrays.asList(null, Subcategory.Other);
            }
        };
        
        public abstract List<Subcategory> subcategories();
    }

    public static enum Subcategory {
        // professional
        OpenSource, Consulting, Education, Marketing,
        // domestic
        Shopping, Housework, Garden, Chores,
        // other
        Other;

        public static List<Subcategory> listFor(final Category category) {
            return category != null? category.subcategories(): Collections.<Subcategory>emptyList();
        }

        static String validate(final Category category, final Subcategory subcategory) {
            if(category == null) {
                return "Enter category first";
            }
            return !category.subcategories().contains(subcategory) 
                    ? "Invalid subcategory for category '" + category + "'" 
                    : null;
        }
        
        public static Predicate<Subcategory> thoseFor(final Category category) {
            return new Predicate<Subcategory>() {

                @Override
                public boolean apply(final Subcategory subcategory) {
                    return category.subcategories().contains(subcategory);
                }
            };
        }
    }

    // //////////////////////////////////////

    private Category category;

    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(
            editing = Editing.DISABLED,
            editingDisabledReason = "Use action to update both category and subcategory"
    )
    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    // //////////////////////////////////////

    private Subcategory subcategory;

    @javax.jdo.annotations.Column(allowsNull="true")
    @Property(
            editing = Editing.DISABLED,
            editingDisabledReason = "Use action to update both category and subcategory"
    )
    public Subcategory getSubcategory() {
        return subcategory;
    }
    public void setSubcategory(final Subcategory subcategory) {
        this.subcategory = subcategory;
    }
    //endregion

    //region > ownedBy (property)

    private String ownedBy;

    @javax.jdo.annotations.Column(allowsNull="false")
    public String getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(final String ownedBy) {
        this.ownedBy = ownedBy;
    }
    //endregion

    //region > complete (property), completed (action), notYetCompleted (action)

    private boolean complete;

    @Property(
        editing = Editing.DISABLED
    )
    public boolean isComplete() {
        return complete;
    }

    public void setComplete(final boolean complete) {
        this.complete = complete;
    }

    @Action(
            domainEvent =CompletedEvent.class,
            invokeOn = InvokeOn.OBJECT_AND_COLLECTION
    )
    public ToDoItem completed() {
        setComplete(true);
        
        //
        // remainder of method just demonstrates the use of the Bulk.InteractionContext service 
        //
        @SuppressWarnings("unused")
        final List<Object> allObjects = actionInvocationContext.getDomainObjects();
        
        LOG.debug("completed: "
                + actionInvocationContext.getIndex() +
                " [" + actionInvocationContext.getSize() + "]"
                + (actionInvocationContext.isFirst() ? " (first)" : "")
                + (actionInvocationContext.isLast() ? " (last)" : ""));

        // if invoked as a regular action, return this object;
        // otherwise (if invoked as bulk), return null (so go back to the list)
        return actionInvocationContext.getInvokedOn() == InvokedOn.OBJECT? this: null;
    }
    // disable action dependent on state of object
    public String disableCompleted() {
        return isComplete() ? "Already completed" : null;
    }

    @Action(
        invokeOn = InvokeOn.OBJECT_AND_COLLECTION
    )
    public ToDoItem notYetCompleted() {
        setComplete(false);

        // if invoked as a regular action, return this object;
        // otherwise (if invoked as bulk), return null (so go back to the list)
        return actionInvocationContext.getInvokedOn() == InvokedOn.OBJECT ? this: null;
    }
    // disable action dependent on state of object
    public String disableNotYetCompleted() {
        return !complete ? "Not yet completed" : null;
    }
    //endregion

    //region > completeSlowly (property)
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Action(
            hidden = Where.EVERYWHERE
    )
    public void completeSlowly(final int millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException ignored) {
        }
        setComplete(true);
    }
    //endregion

    //region > cost (property), updateCost (action)
    private BigDecimal cost;

    @javax.jdo.annotations.Column(allowsNull="true", scale=2)
    @javax.validation.constraints.Digits(integer=10, fraction=2)
    @Property(
            editing = Editing.DISABLED,
            editingDisabledReason = "Update using action"
    )
    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(final BigDecimal cost) {
        this.cost = cost!=null?cost.setScale(2, BigDecimal.ROUND_HALF_EVEN):null;
    }

    @Action(
            semantics = SemanticsOf.IDEMPOTENT
    )
    public ToDoItem updateCost(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "New cost")
            @javax.validation.constraints.Digits(integer=10, fraction=2)
            final BigDecimal cost) {
        LOG.debug("%s: cost updated: %s -> %s", container.titleOf(this), getCost(), cost);
        
        // just to simulate a long-running action
        try {
            Thread.sleep(3000);
        } catch (final InterruptedException ignored) {
        }
        
        setCost(cost);
        return this;
    }

    // provide a default value for argument #0
    public BigDecimal default0UpdateCost() {
        return getCost();
    }
    
    // validate action arguments
    public String validateUpdateCost(final BigDecimal proposedCost) {
        if(proposedCost == null) { return null; }
        return proposedCost.compareTo(BigDecimal.ZERO) < 0? "Cost must be positive": null;
    }
    //endregion

    //region > notes (property)
    private String notes;

    @javax.jdo.annotations.Column(allowsNull="true", length=400)
    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }
    //endregion

    //region > attachment (property)
    private Blob attachment;
    @javax.jdo.annotations.Persistent(defaultFetchGroup="false", columns = {
            @javax.jdo.annotations.Column(name = "attachment_name"),
            @javax.jdo.annotations.Column(name = "attachment_mimetype"),
            @javax.jdo.annotations.Column(name = "attachment_bytes", jdbcType = "BLOB", sqlType = "BLOB")
    })
    @Property(
            optionality = Optionality.OPTIONAL
    )
    public Blob getAttachment() {
        return attachment;
    }

    public void setAttachment(final Blob attachment) {
        this.attachment = attachment;
    }
    //endregion

    //region > doc (property)
    private Clob doc;
    @javax.jdo.annotations.Persistent(defaultFetchGroup="false", columns = {
            @javax.jdo.annotations.Column(name = "doc_name"),
            @javax.jdo.annotations.Column(name = "doc_mimetype"),
            @javax.jdo.annotations.Column(name = "doc_chars", jdbcType = "CLOB", sqlType = "CLOB")
    })
    @Property(
            optionality = Optionality.OPTIONAL
    )
    public Clob getDoc() {
        return doc;
    }

    public void setDoc(final Clob doc) {
        this.doc = doc;
    }
    //endregion

    //region > version (derived property)
    public Long getVersionSequence() {
        if(!(this instanceof javax.jdo.spi.PersistenceCapable)) {
            return null;
        }
        return (Long) JDOHelper.getVersion((javax.jdo.spi.PersistenceCapable) this);
    }
    // hide property (imperatively, based on state of object)
    public boolean hideVersionSequence() {
        return !(this instanceof javax.jdo.spi.PersistenceCapable);
    }
    //endregion

    //region > dependencies (property), add (action), remove (action)

    // overrides the natural ordering
    public static class DependenciesComparator implements Comparator<ToDoItem> {
        @Override
        public int compare(final ToDoItem p, final ToDoItem q) {
            final Ordering<ToDoItem> byDescription = new Ordering<ToDoItem>() {
                public int compare(final ToDoItem p, final ToDoItem q) {
                    return Ordering.natural().nullsFirst().compare(p.getDescription(), q.getDescription());
                }
            };
            return byDescription
                    .compound(Ordering.<ToDoItem>natural())
                    .compare(p, q);
        }
    }

    @javax.jdo.annotations.Persistent(table="ToDoItemDependencies")
    @javax.jdo.annotations.Join(column="dependingId")
    @javax.jdo.annotations.Element(column="dependentId")

    private Set<ToDoItem> dependencies = new TreeSet<>();
    //private SortedSet<ToDoItem> dependencies = new TreeSet<>();  // not compatible with neo4j (as of DN v3.2.3)

    @Collection()
    @CollectionLayout(/*sortedBy = DependenciesComparator.class*/) // not compatible with neo4j (as of DN v3.2.3)
    public Set<ToDoItem> getDependencies() {
        return dependencies;
    }

    public void setDependencies(final Set<ToDoItem> dependencies) {
        this.dependencies = dependencies;
    }

    public void addToDependencies(final ToDoItem toDoItem) {
        getDependencies().add(toDoItem);
    }
    public void removeFromDependencies(final ToDoItem toDoItem) {
        getDependencies().remove(toDoItem);
    }

    public ToDoItem add(
            @ParameterLayout(typicalLength = 20)
            final ToDoItem toDoItem) {
        // By wrapping the call, Isis will detect that the collection is modified
        // and it will automatically send CollectionInteractionEvents to the Event Bus.
        // ToDoItemSubscriptions is a demo subscriber to this event
        wrapperFactory.wrapSkipRules(this).addToDependencies(toDoItem);
        return this;
    }
    public List<ToDoItem> autoComplete0Add(
            @Parameter(minLength = 2)
            final String search) {
        final List<ToDoItem> list = toDoItems.autoComplete(search);
        list.removeAll(getDependencies());
        list.remove(this);
        return list;
    }

    public String disableAdd(final ToDoItem toDoItem) {
        if(isComplete()) {
            return "Cannot add dependencies for items that are complete";
        }
        return null;
    }
    // validate the provided argument prior to invoking action
    public String validateAdd(final ToDoItem toDoItem) {
        if(getDependencies().contains(toDoItem)) {
            return "Already a dependency";
        }
        if(toDoItem == this) {
            return "Can't set up a dependency to self";
        }
        return null;
    }

    public ToDoItem remove(
            @ParameterLayout(typicalLength = 20)
            final ToDoItem toDoItem) {
        // By wrapping the call, Isis will detect that the collection is modified
        // and it will automatically send a CollectionInteractionEvent to the Event Bus.
        // ToDoItemSubscriptions is a demo subscriber to this event
        wrapperFactory.wrapSkipRules(this).removeFromDependencies(toDoItem);
        return this;
    }
    // disable action dependent on state of object
    public String disableRemove(final ToDoItem toDoItem) {
        if(isComplete()) {
            return "Cannot remove dependencies for items that are complete";
        }
        return getDependencies().isEmpty()? "No dependencies to remove": null;
    }
    // validate the provided argument prior to invoking action
    public String validateRemove(final ToDoItem toDoItem) {
        if(!getDependencies().contains(toDoItem)) {
            return "Not a dependency";
        }
        return null;
    }
    // provide a drop-down
    public java.util.Collection<ToDoItem> choices0Remove() {
        return getDependencies();
    }
    //endregion

    //region > clone (action)

    // the name of the action in the UI
    // nb: method is not called "clone()" is inherited by java.lang.Object and
    // (a) has different semantics and (b) is in any case automatically ignored
    // by the framework
    public ToDoItem duplicate(
            @Parameter(regexPattern = "\\w[@&:\\-\\,\\.\\+ \\w]*" )
            @ParameterLayout(named="Description")
            final String description,
            @ParameterLayout(named="Category")
            final Category category,
            @ParameterLayout(named="Subcategory")
            final Subcategory subcategory,
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Due by")
            final LocalDate dueBy,
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Cost")
            final BigDecimal cost) {
        return toDoItems.newToDo(description, category, subcategory, dueBy, cost);
    }
    public String default0Duplicate() {
        return getDescription() + " - Copy";
    }
    public Category default1Duplicate() {
        return getCategory();
    }
    public Subcategory default2Duplicate() {
        return getSubcategory();
    }
    public LocalDate default3Duplicate() {
        return getDueBy();
    }
    public List<Subcategory> choices2Duplicate(
            final String description, final Category category) {
        return toDoItems.choices2NewToDo(description, category);
    }
    public String validateDuplicate(
            final String description, 
            final Category category, final Subcategory subcategory, 
            final LocalDate dueBy, final BigDecimal cost) {
        return toDoItems.validateNewToDo(description, category, subcategory, dueBy, cost);
    }
    //endregion

    //region > delete (action)
    @Action(
            domainEvent =DeletedEvent.class,
            invokeOn = InvokeOn.OBJECT_AND_COLLECTION
    )
    public List<ToDoItem> delete() {
        
        container.removeIfNotAlready(this);

        container.informUser("Deleted " + container.titleOf(this));
        
        // invalid to return 'this' (cannot render a deleted object)
        return toDoItems.notYetComplete();
    }
    //endregion

    //region > totalCost (property)
    @Action(
            semantics = SemanticsOf.SAFE,
            invokeOn = InvokeOn.COLLECTION_ONLY
    )
    public BigDecimal totalCost() {
        BigDecimal total = (BigDecimal) scratchpad.get("runningTotal");
        if(getCost() != null) {
            total = total != null ? total.add(getCost()) : getCost();
            scratchpad.put("runningTotal", total);
        }
        return total.setScale(2, BigDecimal.ROUND_HALF_EVEN);
    }
    //endregion

    //region > openSourceCodeOnGithub (action)
    @Action(
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    public URL openSourceCodeOnGithub() throws MalformedURLException {
        return new URL("https://github.com/apache/isis/tree/master/example/application/todoapp/dom/src/main/java/dom/todo/ToDoItem.java");
    }
    //endregion

    //region > demoException (action)

    static enum DemoExceptionType {
        RecoverableException,
        RecoverableExceptionAutoEscalated,
        NonRecoverableException;
    }

    @Action(
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    public void demoException(
            @ParameterLayout(named="Type")
            final DemoExceptionType type) {
        switch(type) {
        case NonRecoverableException:
            throw new NonRecoverableException("Demo throwing " + type.name());
        case RecoverableException:
            throw new RecoverableException("Demo throwing " + type.name());
        case RecoverableExceptionAutoEscalated:
            try {
                // this will trigger an exception (because category cannot be null), causing the xactn to be aborted
                setCategory(null);
                container.flush();
            } catch(Exception e) {
                // it's a programming mistake to throw only a recoverable exception here, because of the xactn's state.
                // the framework should instead auto-escalate this to a non-recoverable exception
                throw new RecoverableException("Demo throwing " + type.name(), e);
            }
        }
    }
    //endregion

    //region > lifecycle callbacks

    public void created() {
        LOG.debug("lifecycle callback: created: " + this.toString());
    }
    public void loaded() {
        LOG.debug("lifecycle callback: loaded: " + this.toString());
    }
    public void persisting() {
        LOG.debug("lifecycle callback: persisting: " + this.toString());
    }
    public void persisted() {
        LOG.debug("lifecycle callback: persisted: " + this.toString());
    }
    public void updating() {
        LOG.debug("lifecycle callback: updating: " + this.toString());
    }
    public void updated() {
        LOG.debug("lifecycle callback: updated: " + this.toString());
    }
    public void removing() {
        LOG.debug("lifecycle callback: removing: " + this.toString());
    }
    public void removed() {
        LOG.debug("lifecycle callback: removed: " + this.toString());
    }
    //endregion

    //region > object-level validation

    /**
     * Prevent user from viewing another user's data.
     */
    public boolean hidden() {
        // uncomment to enable.  As things stand, the disabled() method below instead will make object "read-only".
        //return !Objects.equal(getOwnedBy(), container.getUser().getName());
        return false;
    }

    /**
     * Prevent user from modifying any other user's data.
     */
    public String disabled(final Identifier.Type identifierType){
        final UserMemento currentUser = container.getUser();
        final String currentUserName = currentUser.getName();
        if(Objects.equal(getOwnedBy(), currentUserName)) { return null; }
        return "This object is owned by '" + getOwnedBy() + "' and cannot be modified by you";
    }

    /**
     * In a real app, if this were actually a rule, then we'd expect that
     * invoking the {@link #completed() done} action would clear the {@link #getDueBy() dueBy}
     * property (rather than require the user to have to clear manually).
     */
    public String validate() {
        if(isComplete() && getDueBy() != null) {
            return "Due by date must be set to null if item has been completed";
        }
        return null;
    }


    //endregion


    //region > programmatic helpers
    @Programmatic // excluded from the framework's metamodel
    public boolean isDue() {
        if (getDueBy() == null) {
            return false;
        }
        return !toDoItems.isMoreThanOneWeekInPast(getDueBy());
    }
    //endregion

    //region > events

    public static abstract class AbstractActionDomainEvent extends ActionDomainEvent<ToDoItem> {
        private static final long serialVersionUID = 1L;
        private final String description;
        public AbstractActionDomainEvent(
                final String description,
                final ToDoItem source,
                final Identifier identifier,
                final Object... arguments) {
            super(source, identifier, arguments);
            this.description = description;
        }
        public String getEventDescription() {
            return description;
        }
    }

    public static class CompletedEvent extends AbstractActionDomainEvent {
        private static final long serialVersionUID = 1L;
        public CompletedEvent(
                final ToDoItem source, 
                final Identifier identifier, 
                final Object... arguments) {
            super("completed", source, identifier, arguments);
        }
    }

    public static class NoLongerCompletedEvent extends AbstractActionDomainEvent {
        private static final long serialVersionUID = 1L;
        public NoLongerCompletedEvent(
                final ToDoItem source, 
                final Identifier identifier, 
                final Object... arguments) {
            super("no longer completed", source, identifier, arguments);
        }
    }

    public static class DeletedEvent extends AbstractActionDomainEvent {
        private static final long serialVersionUID = 1L;
        public DeletedEvent(
                final ToDoItem source, 
                final Identifier identifier, 
                final Object... arguments) {
            super("deleted", source, identifier, arguments);
        }
    }

    //endregion

    //region > predicates

    public static class Predicates {
        
        public static Predicate<ToDoItem> thoseOwnedBy(final String currentUser) {
            return new Predicate<ToDoItem>() {
                @Override
                public boolean apply(final ToDoItem toDoItem) {
                    return Objects.equal(toDoItem.getOwnedBy(), currentUser);
                }
            };
        }

        public static Predicate<ToDoItem> thoseCompleted(
                final boolean completed) {
            return new Predicate<ToDoItem>() {
                @Override
                public boolean apply(final ToDoItem t) {
                    return Objects.equal(t.isComplete(), completed);
                }
            };
        }

        public static Predicate<ToDoItem> thoseWithSimilarDescription(final String description) {
            return new Predicate<ToDoItem>() {
                @Override
                public boolean apply(final ToDoItem t) {
                    return t.getDescription().contains(description);
                }
            };
        }

        @SuppressWarnings("unchecked")
        public static Predicate<ToDoItem> thoseSimilarTo(final ToDoItem toDoItem) {
            return com.google.common.base.Predicates.and(
                    thoseNot(toDoItem),
                    thoseOwnedBy(toDoItem.getOwnedBy()),
                    thoseCategorised(toDoItem.getCategory()));
        }

        public static Predicate<ToDoItem> thoseNot(final ToDoItem toDoItem) {
            return new Predicate<ToDoItem>() {
                @Override
                public boolean apply(final ToDoItem t) {
                    return t != toDoItem;
                }
            };
        }

        public static Predicate<ToDoItem> thoseCategorised(final Category category) {
            return new Predicate<ToDoItem>() {
                @Override
                public boolean apply(final ToDoItem toDoItem) {
                    return Objects.equal(toDoItem.getCategory(), category);
                }
            };
        }

        public static Predicate<ToDoItem> thoseSubcategorised(
                final Subcategory subcategory) {
            return new Predicate<ToDoItem>() {
                @Override
                public boolean apply(final ToDoItem t) {
                    return Objects.equal(t.getSubcategory(), subcategory);
                }
            };
        }

        public static Predicate<ToDoItem> thoseCategorised(
                final Category category, final Subcategory subcategory) {
            return com.google.common.base.Predicates.and(
                    thoseCategorised(category), 
                    thoseSubcategorised(subcategory)); 
        }

    }

    //endregion

    //region > toString, compareTo
    @Override
    public String toString() {
        return ObjectContracts.toString(this, "description,complete,dueBy,ownedBy");
    }

    /**
     * Required so can store in {@link SortedSet sorted set}s (eg {@link #getDependencies()}). 
     */
    @Override
    public int compareTo(final ToDoItem other) {
        return ObjectContracts.compare(this, other, "complete,dueBy,description");
    }
    //endregion

    //region > injected services
    @javax.inject.Inject
    DomainObjectContainer container;

    @javax.inject.Inject
    ToDoItems toDoItems;

    @javax.inject.Inject
    Scratchpad scratchpad;

    /**
     * public only so can be injected from integ tests
     */
    @javax.inject.Inject
    public ActionInvocationContext actionInvocationContext;

    /**
     * public only so can be injected from integ tests
     */
    @javax.inject.Inject
    public EventBusService eventBusService;

    @javax.inject.Inject
    WrapperFactory wrapperFactory;

    //endregion

}
