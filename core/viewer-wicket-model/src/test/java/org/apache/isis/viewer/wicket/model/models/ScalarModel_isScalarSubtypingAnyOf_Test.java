package org.apache.isis.viewer.wicket.model.models;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ScalarModel_isScalarSubtypingAnyOf_Test {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    ObjectSpecification mockObjectSpecification;

    public static class A {}
    public static class B extends A {}
    public static class C extends B {}

    @org.junit.Test
    public void when_super() {
        assertThat(newScalarModelFor(A.class).isScalarTypeSubtypeOf(B.class), is(equalTo(false)));
    }

    @org.junit.Test
    public void when_same() {
        assertThat(newScalarModelFor(B.class).isScalarTypeSubtypeOf(B.class), is(equalTo(true)));
    }

    @org.junit.Test
    public void when_sub() {
        assertThat(newScalarModelFor(C.class).isScalarTypeSubtypeOf(B.class), is(equalTo(true)));
    }

    private ScalarModel newScalarModelFor(final Class<?> result) {
        final ScalarModel scalarModel = new ScalarModel(null, null) {
            @Override public ObjectSpecification getTypeOfSpecification() {
                return mockObjectSpecification;
            }
        };
        context.checking(new Expectations() {{
            allowing(mockObjectSpecification).getCorrespondingClass();
            will(returnValue(result));
        }});
        return scalarModel;
    }
}