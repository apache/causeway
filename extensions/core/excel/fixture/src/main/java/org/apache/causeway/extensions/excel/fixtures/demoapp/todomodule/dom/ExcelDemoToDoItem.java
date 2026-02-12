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
package org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

import jakarta.inject.Inject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.MinLength;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.title.TitleService;
import org.apache.causeway.applib.util.TitleBuffer;
import org.apache.causeway.applib.value.Blob;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
        schema = "libExcelFixture"
)
@DomainObject(
        autoCompleteRepository = ExcelDemoToDoItemMenu.class
)
@DomainObjectLayout(
        named = "To Do Item",
        bookmarking = BookmarkPolicy.AS_ROOT
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@ToString(of = {
        "description",
        "complete",
        "dueBy",
        "ownedBy"
})
public class ExcelDemoToDoItem implements Comparable<ExcelDemoToDoItem> /*, CalendarEventable, Locatable*/ {

    public static final String FQCN = "org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem";

    @Inject transient private MessageService messageService;
    @Inject transient private RepositoryService repositoryService;
    @Inject transient private TitleService titleService;
    @Inject transient private ExcelDemoToDoItemMenu toDoItems;
    @Inject transient private ClockService clockService;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    long id;

    //region > title, iconName

    @ObjectSupport public String title() {
        final TitleBuffer buf = new TitleBuffer();
        buf.append(getDescription());
        if (isComplete()) {
            buf.append("- Completed!");
        } else {
            if (getDueBy() != null) {
                buf.append(" due by", getDueBy());
            }
        }
        return buf.toString();
    }

    @ObjectSupport public String iconName() {
        return "ExcelModuleDemoToDoItem-" + (!isComplete() ? "todo" : "done");
    }

    //endregion

    @Column(nullable=false, length=100)
    @Property(regexPattern = "\\w[@&:\\-\\,\\.\\+ \\w]*")
    @Getter @Setter
    private String description;

    @Getter @Setter
    private LocalDate dueBy;

    @Column(nullable=true)
    @Getter @Setter
    private Category category;

    @Column(nullable=true)
    @Getter @Setter
    private Subcategory subcategory;

    @Column(nullable=false)
    @Getter @Setter
    private String ownedBy;

    @Property(editing = Editing.DISABLED)
    @Getter @Setter
    private boolean complete;

    @Column(nullable=true, scale=2)
    @Property(editing = Editing.DISABLED, editingDisabledReason = "Update using action")
    @Getter @Setter
    private BigDecimal cost;

    @Column(nullable=true, scale=2)
    @Property(
            editing = Editing.DISABLED,
            editingDisabledReason = "Update using action"
    )
    @Getter @Setter
    private BigDecimal previousCost;

    @Getter @Setter
    @Column(nullable=true, length=400)
    @Property(editing = Editing.ENABLED)
    private String notes;

    @Getter @Setter
    private Blob attachment;

    @Getter @Setter
    @CollectionLayout(sortedBy = DependenciesComparator.class)
    private SortedSet<ExcelDemoToDoItem> dependencies = new TreeSet<>();

    // no getter/setter (therefore persisted but not part of Causeway' metamodel)
    private Double locationLatitude;
    private Double locationLongitude;

    public String validateDueBy(final LocalDate dueBy) {
        if (dueBy == null)
            return null;
        return isMoreThanOneWeekInPast(dueBy) ? "Due by date cannot be more than one week old" : null;
    }

    //region > completed (action)

    public ExcelDemoToDoItem completed() {
        setComplete(true);
        return this;
    }
    public String disableCompleted() {
        return isComplete() ? "Already completed" : null;
    }

    //endregion

    //region > notYetCompleted (action)

    public ExcelDemoToDoItem notYetCompleted() {
        setComplete(false);

        return this;
    }
    // disable action dependent on state of object
    public String disableNotYetCompleted() {
        return !complete ? "Not yet completed" : null;
    }

    //endregion

    //region > updateCosts (action)
    public ExcelDemoToDoItem updateCosts(
            @Nullable
            @jakarta.validation.constraints.Digits(integer=10, fraction=2)
            final BigDecimal cost,

            @Nullable
            @jakarta.validation.constraints.Digits(integer=10, fraction=2)
            final BigDecimal previousCost
    ) {
        setCost(cost);
        setPreviousCost(previousCost);
        return this;
    }
    public BigDecimal default0UpdateCosts() {
        return getCost();
    }
    public BigDecimal default1UpdateCosts() {
        return getPreviousCost();
    }
    public String validateUpdateCosts(final BigDecimal proposedCost, final BigDecimal proposedPreviousCost) {
        if (proposedCost != null && proposedCost.compareTo(BigDecimal.ZERO) < 0)
            return "Cost must be positive";
        if (proposedPreviousCost != null && proposedPreviousCost.compareTo(BigDecimal.ZERO) < 0)
            return "Previous cost must be positive";
        return null;
    }
    //endregion

    //region > add (action)

    public ExcelDemoToDoItem add(
            final ExcelDemoToDoItem toDoItem) {
        getDependencies().add(toDoItem);
        return this;
    }
    public List<ExcelDemoToDoItem> autoComplete0Add(final @MinLength(2) String search) {
        final List<ExcelDemoToDoItem> list = toDoItems.autoComplete(search);
        list.removeAll(getDependencies());
        list.remove(this);
        return list;
    }

    public String disableAdd() {
        if(isComplete())
            return "Cannot add dependencies for items that are complete";
        return null;
    }
    // validate the provided argument prior to invoking action
    public String validateAdd(final ExcelDemoToDoItem toDoItem) {
        if(getDependencies().contains(toDoItem))
            return "Already a dependency";
        if(toDoItem == this)
            return "Can't set up a dependency to self";
        return null;
    }
    //endregion

    //region > remove (action)

    public ExcelDemoToDoItem remove(
            final ExcelDemoToDoItem toDoItem) {
        getDependencies().remove(toDoItem);
        return this;
    }
    // disable action dependent on state of object
    public String disableRemove() {
        if(isComplete())
            return "Cannot remove dependencies for items that are complete";
        return getDependencies().isEmpty()? "No dependencies to remove": null;
    }
    // validate the provided argument prior to invoking action
    public String validateRemove(final ExcelDemoToDoItem toDoItem) {
        if(!getDependencies().contains(toDoItem))
            return "Not a dependency";
        return null;
    }
    // provide a drop-down
    public SortedSet<ExcelDemoToDoItem> choices0Remove() {
        return getDependencies();
    }

    //endregion

    //region > delete (action)

    @Action(semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
    public List<ExcelDemoToDoItem> delete() {
        repositoryService.remove(this);
        messageService.informUser("Deleted " + titleService.titleOf(this));
        // invalid to return 'this' (cannot render a deleted object)
        return toDoItems.toDoItemsNotYetComplete();
    }

    //endregion

    //region > Programmatic Helpers
    private static final long ONE_WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000L;

    private boolean isMoreThanOneWeekInPast(final LocalDate dueBy) {

        long epochMillisAtStartOfDay =
                dueBy.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        return epochMillisAtStartOfDay < (clockService.getEpochMillis() - ONE_WEEK_IN_MILLIS);
    }

    //endregion

    public static class Predicates {

        public static Predicate<ExcelDemoToDoItem> thoseOwnedBy(final String currentUser) {
            return toDoItem -> Objects.equals(toDoItem.getOwnedBy(), currentUser);
        }

        public static Predicate<ExcelDemoToDoItem> thoseCompleted(
                final boolean completed) {
            return t -> Objects.equals(t.isComplete(), completed);
        }

        public static Predicate<ExcelDemoToDoItem> thoseCategorised(final Category category) {
            return toDoItem -> Objects.equals(toDoItem.getCategory(), category);
        }

        public static Predicate<ExcelDemoToDoItem> thoseSubcategorised(
                final Subcategory subcategory) {
            return t -> Objects.equals(t.getSubcategory(), subcategory);
        }

        public static Predicate<ExcelDemoToDoItem> thoseCategorised(
                final Category category, final Subcategory subcategory) {

            return
                    thoseCategorised(category)
                    .and(thoseSubcategorised(subcategory));
        }

    }

    private static final Comparator<ExcelDemoToDoItem> comparator =
            Comparator.comparing(ExcelDemoToDoItem::isComplete).reversed() // true first
                .thenComparing(ExcelDemoToDoItem::getDueBy)
                .thenComparing(ExcelDemoToDoItem::getDescription);

    @Override
    public int compareTo(final ExcelDemoToDoItem other) {
        return comparator.compare(this, other);
    }

    //endregion

//    @Programmatic
//    @Override
//    public String getCalendarName() {
//        return getCategory().name();
//    }
//
//    @Programmatic
//    @Override
//    public CalendarEvent toCalendarEvent() {
//        if(getDueBy() == null) {
//            return null;
//        }
//        return new CalendarEvent(getDueBy().toDateTimeAtStartOfDay(), getCalendarName(), container.titleOf(this));
//    }

//    @Property(
//            optionality = Optionality.OPTIONAL,
//            editing = Editing.DISABLED
//    )
//    @PropertyLayout(sequence="3")
//    public Location getLocation() {
//        return locationLatitude != null && locationLongitude != null? new Location(locationLatitude, locationLongitude): null;
//    }
//    public void setLocation(final Location location) {
//        locationLongitude = location != null ? location.getLongitude() : null;
//        locationLatitude = location != null ? location.getLatitude() : null;
//    }
//
//    @PropertyLayout(group="location", sequence="1")
//    public ExcelDemoToDoItem updateLocation(final Double longitude, final Double latitude) {
//        locationLatitude = latitude;
//        locationLongitude = longitude;
//        return this;
//    }

}
