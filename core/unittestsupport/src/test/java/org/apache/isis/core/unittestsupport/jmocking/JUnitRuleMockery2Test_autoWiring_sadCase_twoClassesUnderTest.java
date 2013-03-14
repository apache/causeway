package org.apache.isis.core.unittestsupport.jmocking;

import org.jmock.auto.Mock;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.ClassUnderTest;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class JUnitRuleMockery2Test_autoWiring_sadCase_twoClassesUnderTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private Collaborator collaborator;

    @ClassUnderTest
	private Collaborating collaborating;

    @ClassUnderTest
	private Collaborating anotherCollaborating;

    @Ignore("This isn't actually possible to test, because the test is actually thrown by the rule, which is further up the callstack than the test method")
    @Test(expected=AssertionError.class)
    public void cannotFindClassUnderTest() {
    	context.getClassUnderTest();
    }

}
