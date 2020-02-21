package org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.fixturescripts;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.inject.Inject;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.persistence.jdo.applib.services.IsisJdoSupport;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Category;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Subcategory;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

public class ExcelDemoToDoItem_recreate5_for extends FixtureScript {

    private final String user;

    public ExcelDemoToDoItem_recreate5_for() {
        this(null);
    }
    
    public ExcelDemoToDoItem_recreate5_for(String ownedBy) {
        this.user = ownedBy;
    }


    @Override
    protected void execute(ExecutionContext executionContext) {

        final String ownedBy = this.user != null? this.user : userService.getUser().getName();

        executionContext.executeChild(this, new ExcelDemoToDoItem_tearDown2(ownedBy));

        installFor(ownedBy, executionContext);
        
        transactionService.flushTransaction();
    }

    private void installFor(String user, ExecutionContext executionContext) {

        ExcelDemoToDoItem t1 = createToDoItemForUser("Buy milk", Category.Domestic, Subcategory.Shopping, user, daysFromToday(0), new BigDecimal("0.75"), executionContext);
        ExcelDemoToDoItem t2 = createToDoItemForUser("Buy bread", Category.Domestic, Subcategory.Shopping, user, daysFromToday(0), new BigDecimal("1.75"), executionContext);
        ExcelDemoToDoItem t3 = createToDoItemForUser("Buy stamps", Category.Domestic, Subcategory.Shopping, user, daysFromToday(0), new BigDecimal("10.00"), executionContext);
        t3.setComplete(true);
        ExcelDemoToDoItem t4 = createToDoItemForUser("Pick up laundry", Category.Domestic, Subcategory.Chores, user, daysFromToday(6), new BigDecimal("7.50"), executionContext);
        ExcelDemoToDoItem t5 = createToDoItemForUser("Mow lawn", Category.Domestic, Subcategory.Garden, user, daysFromToday(6), null, executionContext);

        createToDoItemForUser("Vacuum house", Category.Domestic, Subcategory.Housework, user, daysFromToday(3), null, executionContext);
        createToDoItemForUser("Sharpen knives", Category.Domestic, Subcategory.Chores, user, daysFromToday(14), null, executionContext);
        
        createToDoItemForUser("Write to penpal", Category.Other, Subcategory.Other, user, null, null, executionContext);
        
        createToDoItemForUser("Write blog post", Category.Professional, Subcategory.Marketing, user, daysFromToday(7), null, executionContext).setComplete(true);
        createToDoItemForUser("Organize brown bag", Category.Professional, Subcategory.Consulting, user, daysFromToday(14), null, executionContext);
        createToDoItemForUser("Submit conference session", Category.Professional, Subcategory.Education, user, daysFromToday(21), null, executionContext);
        createToDoItemForUser("Stage Isis release", Category.Professional, Subcategory.OpenSource, user, null, null, executionContext);

        t1.add(t2);
        t1.add(t3);
        t1.add(t4);
        t1.add(t5);
        
        t2.add(t3);
        t2.add(t4);
        t2.add(t5);
        
        t3.add(t4);

        transactionService.flushTransaction();
    }


    // //////////////////////////////////////

    private ExcelDemoToDoItem createToDoItemForUser(final String description, final Category category, Subcategory subcategory, String user, final LocalDate dueBy, final BigDecimal cost, ExecutionContext executionContext) {
        final ExcelDemoToDoItem toDoItem = demoToDoItemMenu.newToDoItem(description, category, subcategory, user, dueBy, cost);
        executionContext.addResult(this, toDoItem);
        return toDoItem;
    }

    private LocalDate daysFromToday(final int i) {
        final LocalDate date = clockService.now();
        return date.plusDays(i);
    }


    @Inject private ExcelDemoToDoItemMenu demoToDoItemMenu;
    @Inject private IsisJdoSupport isisJdoSupport;
    @Inject private ClockService clockService;

}
