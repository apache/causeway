package org.apache.isis.core.unittestsupport.jmocking;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Allowing;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.ClassUnderTest;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class JUnitRuleMockery2Test_mockAnnotatedWithAllowing {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Allowing
    @Mock
    private Collaborator collaborator;

    @ClassUnderTest
	private Collaborating collaborating;

    @Before
	public void setUp() throws Exception {
    	collaborating = (Collaborating) context.getClassUnderTest();
	}
    
    @Test
    public void invocationOnCollaboratorIsIgnored() {
    	collaborating.collaborateWithCollaborator();
    }

    @Test
    public void lackOfInvocationOnCollaboratorIsIgnored() {
    	collaborating.dontCollaborateWithCollaborator();
    }


}
