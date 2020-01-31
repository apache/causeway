package org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.fixturehandlers.excelupload;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.util.JaxbAdapters;
import org.apache.isis.subdomains.excel.testing.ExcelFixture;
import org.apache.isis.subdomains.excel.testing.ExcelFixtureRowHandler;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Category;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Subcategory;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import lombok.Getter;
import lombok.Setter;

@DomainObject(
        objectType = "libExcelFixture.ExcelUploadRowHandler4ToDoItem",
        nature = Nature.VIEW_MODEL
)
@XmlRootElement(name = "BulkUpdateLineItemForDemoToDoItem")
@XmlType(
        propOrder = {
                "description",
                "subCategory",
                "ownedBy",
                "dueBy",
                "cost",
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
public class ExcelUploadRowHandler4ToDoItem implements ExcelFixtureRowHandler {

    @Getter @Setter
    private String description;

    @Getter @Setter
    private String subCategory;

    @Getter @Setter
    private String ownedBy;

    @Getter @Setter @XmlJavaTypeAdapter(JaxbAdapters.LocalDateAdapter.class)
    private LocalDate dueBy;

    @Getter @Setter
    private BigDecimal cost;

    @Override
    public List<Object> handleRow(final FixtureScript.ExecutionContext executionContext, final ExcelFixture excelFixture, final Object previousRow) {
        final ExcelDemoToDoItem toDoItem = toDoItems.newToDoItem(
                description,
                Category.Professional,
                Subcategory.valueOf(subCategory),
                ownedBy,
                dueBy,
                cost);
        executionContext.addResult(excelFixture, toDoItem);
        return Collections.<Object>singletonList(toDoItem);
    }

    @Inject @XmlTransient
    ExcelDemoToDoItemMenu toDoItems;

    @Inject @XmlTransient
    WrapperFactory wrapperFactory;
}
