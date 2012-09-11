package org.apache.isis.core.metamodel.feature;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;

@RunWith(Parameterized.class)
public class ObjectAssociationFiltersTest_visibleWhere {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);
    
    @Mock
    private ObjectAssociation mockObjectAssociation;

    @Mock
    private HiddenFacet mockHiddenFacet;

    // given
    private When when;
    private Where where;

    // when
    private Where whereContext;

    // then
    private boolean expectedVisibility;

    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {When.ALWAYS, Where.ANYWHERE, Where.ANYWHERE, false},
                {When.UNTIL_PERSISTED, Where.ANYWHERE, Where.ANYWHERE, true},
                {When.ONCE_PERSISTED, Where.ANYWHERE, Where.ANYWHERE, true},
                {When.NEVER, Where.ANYWHERE, Where.ANYWHERE, true},
                {When.ALWAYS, Where.OBJECT_FORMS, Where.OBJECT_FORMS, false},
                {When.ALWAYS, Where.OBJECT_FORMS, Where.ALL_TABLES, true},
                {When.ALWAYS, Where.OBJECT_FORMS, Where.PARENTED_TABLES, true},
                {When.ALWAYS, Where.OBJECT_FORMS, Where.STANDALONE_TABLES, true},
                {When.ALWAYS, Where.STANDALONE_TABLES, Where.OBJECT_FORMS, true},
                {When.ALWAYS, Where.STANDALONE_TABLES, Where.PARENTED_TABLES, true},
                {When.ALWAYS, Where.STANDALONE_TABLES, Where.STANDALONE_TABLES, false},
                {When.ALWAYS, Where.PARENTED_TABLES, Where.OBJECT_FORMS, true},
                {When.ALWAYS, Where.PARENTED_TABLES, Where.PARENTED_TABLES, false},
                {When.ALWAYS, Where.PARENTED_TABLES, Where.STANDALONE_TABLES, true},
                {When.ALWAYS, Where.ALL_TABLES, Where.OBJECT_FORMS, true},
                {When.ALWAYS, Where.ALL_TABLES, Where.PARENTED_TABLES, false},
                {When.ALWAYS, Where.ALL_TABLES, Where.STANDALONE_TABLES, false},
                });
    }

    public ObjectAssociationFiltersTest_visibleWhere(When when, Where where, Where context, boolean visible) {
        this.when = when;
        this.where = where;
        this.whereContext = context;
        this.expectedVisibility = visible;
    }
    
    @Before
    public void setUp() throws Exception {
        context.checking(new Expectations(){{
            one(mockObjectAssociation).getFacet(HiddenFacet.class);
            will(returnValue(mockHiddenFacet));
            
            allowing(mockHiddenFacet).where();
            will(returnValue(where));

            allowing(mockHiddenFacet).when();
            will(returnValue(when));
        }});
    }
    
    @Test
    public void test() {
        final Filter<ObjectAssociation> filter = ObjectAssociationFilters.staticallyVisible(whereContext);
        assertThat(filter.accept(mockObjectAssociation), is(expectedVisibility));
    }

}
