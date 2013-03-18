package org.apache.isis.core.unittestsupport.jmocking;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Checking;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.ClassUnderTest;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.ExpectationsOn;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class JUnitRuleMockery2Test_mockAnnotatedWithChecking {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    public static class ExpectCall extends ExpectationsOn<Collaborator> {
    	public ExpectCall(Object mock) {
			super(mock);
		}
	{
    	oneOf(mock()).doOtherStuff();
    }}

    @Checking(ExpectCall.class)
    @Mock
    private Collaborator collaborator;

    @ClassUnderTest
	private CollaboratingUsingConstructorInjection collaborating;

    @Before
	public void setUp() throws Exception {
    	collaborating = (CollaboratingUsingConstructorInjection) context.getClassUnderTest();
	}
    
    @Test
    public void invocationOnCollaboratorIsIgnored() {
    	collaborating.collaborateWithCollaborator();
    }
}
