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
                {When.ALWAYS, Where.OBJECT_FORM, Where.OBJECT_FORM, false},
                {When.ALWAYS, Where.OBJECT_FORM, Where.ALL_TABLES, true},
                {When.ALWAYS, Where.OBJECT_FORM, Where.PARENTED_TABLE, true},
                {When.ALWAYS, Where.OBJECT_FORM, Where.STANDALONE_TABLE, true},
                {When.ALWAYS, Where.STANDALONE_TABLE, Where.OBJECT_FORM, true},
                {When.ALWAYS, Where.STANDALONE_TABLE, Where.PARENTED_TABLE, true},
                {When.ALWAYS, Where.STANDALONE_TABLE, Where.STANDALONE_TABLE, false},
                {When.ALWAYS, Where.PARENTED_TABLE, Where.OBJECT_FORM, true},
                {When.ALWAYS, Where.PARENTED_TABLE, Where.PARENTED_TABLE, false},
                {When.ALWAYS, Where.PARENTED_TABLE, Where.STANDALONE_TABLE, true},
                {When.ALWAYS, Where.ALL_TABLES, Where.OBJECT_FORM, true},
                {When.ALWAYS, Where.ALL_TABLES, Where.PARENTED_TABLE, false},
                {When.ALWAYS, Where.ALL_TABLES, Where.STANDALONE_TABLE, false},
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
