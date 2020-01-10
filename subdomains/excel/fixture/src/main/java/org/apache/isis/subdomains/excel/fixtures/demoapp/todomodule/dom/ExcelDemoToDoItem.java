package org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.VersionStrategy;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.jaxbadapters.PersistentEntityAdapter;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.applib.value.Blob;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.DATASTORE,
        schema = "libExcelFixture"
)
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
            name = "todo_all", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem "
                    + "WHERE ownedBy == :ownedBy"),
    @javax.jdo.annotations.Query(
            name = "todo_notYetComplete", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem "
                    + "WHERE ownedBy == :ownedBy "
                    + "   && complete == false"),
    @javax.jdo.annotations.Query(
            name = "findByDescription", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem "
                    + "WHERE ownedBy == :ownedBy "
                    + "   && description == :description"),
    @javax.jdo.annotations.Query(
            name = "todo_complete", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem "
                    + "WHERE ownedBy == :ownedBy "
                    + "&& complete == true"),
    @javax.jdo.annotations.Query(
            name = "todo_similarTo", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem "
                    + "WHERE ownedBy == :ownedBy "
                    + "&& category == :category"),
    @javax.jdo.annotations.Query(
            name = "todo_autoComplete", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem "
                    + "WHERE ownedBy == :ownedBy && "
                    + "description.indexOf(:description) >= 0")
})
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

    //@Inject private UserService userService;
    @Inject private MessageService messageService;
    @Inject private RepositoryService repositoryService;
    @Inject private TitleService titleService;
    @Inject private ExcelDemoToDoItemMenu toDoItems;
    
    //region > title, iconName

    public String title() {
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
    
    public String iconName() {
        return "ExcelModuleDemoToDoItem-" + (!isComplete() ? "todo" : "done");
    }

    //endregion

    @javax.jdo.annotations.Column(allowsNull="false", length=100)
    @Property(regexPattern = "\\w[@&:\\-\\,\\.\\+ \\w]*")
    @Getter @Setter
    private String description;

    @javax.jdo.annotations.Persistent(defaultFetchGroup="true")
    @javax.jdo.annotations.Column(allowsNull="true")
    @Getter @Setter
    private LocalDate dueBy;

    @javax.jdo.annotations.Column(allowsNull="true")
    @Getter @Setter
    private Category category;

    @javax.jdo.annotations.Column(allowsNull="true")
    @Getter @Setter
    private Subcategory subcategory;

    @javax.jdo.annotations.Column(allowsNull="false")
    @Getter @Setter
    private String ownedBy;

    @Property(editing = Editing.DISABLED)
    @Getter @Setter
    private boolean complete;


    @javax.jdo.annotations.Column(allowsNull="true", scale=2)
    //XXX breaks build, as of JDOQueryProcessor generated source, that cannot be compiled
    //@javax.validation.constraints.Digits(integer=10, fraction=2)
    @Property(editing = Editing.DISABLED, editingDisabledReason = "Update using action")
    @Getter @Setter
    private BigDecimal cost;

    @javax.jdo.annotations.Column(allowsNull="true", scale=2)
    //XXX breaks build, as of JDOQueryProcessor generated source, that cannot be compiled
    //@javax.validation.constraints.Digits(integer=10, fraction=2)
    @Property(
            editing = Editing.DISABLED,
            editingDisabledReason = "Update using action"
    )
    @Getter @Setter
    private BigDecimal previousCost;


    @Getter @Setter
    @javax.jdo.annotations.Column(allowsNull="true", length=400)
    @Property(editing = Editing.ENABLED)
    // @SummernoteEditor(height = 100, maxHeight = 300)
    private String notes;

    @Getter @Setter
    @javax.jdo.annotations.Persistent(defaultFetchGroup="false")
    @javax.jdo.annotations.Column(allowsNull="true", jdbcType="BLOB", sqlType="LONGBINARY")
    private Blob attachment;

    @Getter @Setter
    @javax.jdo.annotations.Persistent(table="ExcelDemoToDoItemDependencies")
    @javax.jdo.annotations.Join(column="dependingId")
    @javax.jdo.annotations.Element(column="dependentId")
    @CollectionLayout(sortedBy = DependenciesComparator.class)
    private SortedSet<ExcelDemoToDoItem> dependencies = new TreeSet<>();



    // no getter/setter (therefore persisted but not part of Isis' metamodel)
    private Double locationLatitude;
    private Double locationLongitude;


    public String validateDueBy(final LocalDate dueBy) {
        if (dueBy == null) {
            return null;
        }
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
            @javax.validation.constraints.Digits(integer=10, fraction=2)
            final BigDecimal cost,
            
            @Nullable
            @javax.validation.constraints.Digits(integer=10, fraction=2)
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
        if (proposedCost != null && proposedCost.compareTo(BigDecimal.ZERO) < 0) {
            return "Cost must be positive";
        }
        if (proposedPreviousCost != null && proposedPreviousCost.compareTo(BigDecimal.ZERO) < 0) {
            return "Previous cost must be positive";
        }
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
        if(isComplete()) {
            return "Cannot add dependencies for items that are complete";
        }
        return null;
    }
    // validate the provided argument prior to invoking action
    public String validateAdd(final ExcelDemoToDoItem toDoItem) {
        if(getDependencies().contains(toDoItem)) {
            return "Already a dependency";
        }
        if(toDoItem == this) {
            return "Can't set up a dependency to self";
        }
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
        if(isComplete()) {
            return "Cannot remove dependencies for items that are complete";
        }
        return getDependencies().isEmpty()? "No dependencies to remove": null;
    }
    // validate the provided argument prior to invoking action
    public String validateRemove(final ExcelDemoToDoItem toDoItem) {
        if(!getDependencies().contains(toDoItem)) {
            return "Not a dependency";
        }
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

    private static boolean isMoreThanOneWeekInPast(final LocalDate dueBy) {
        
        long epochMillisAtStartOfDay = 
                dueBy.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                
        return epochMillisAtStartOfDay < (Clock.getEpochMillis() - ONE_WEEK_IN_MILLIS);
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
       
    private final static Comparator<ExcelDemoToDoItem> comparator = 
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
//    @MemberOrder(sequence="3")
//    public Location getLocation() {
//        return locationLatitude != null && locationLongitude != null? new Location(locationLatitude, locationLongitude): null;
//    }
//    public void setLocation(final Location location) {
//        locationLongitude = location != null ? location.getLongitude() : null;
//        locationLatitude = location != null ? location.getLatitude() : null;
//    }
//
//    @MemberOrder(name="location", sequence="1")
//    public ExcelDemoToDoItem updateLocation(final Double longitude, final Double latitude) {
//        locationLatitude = latitude;
//        locationLongitude = longitude;
//        return this;
//    }


}
