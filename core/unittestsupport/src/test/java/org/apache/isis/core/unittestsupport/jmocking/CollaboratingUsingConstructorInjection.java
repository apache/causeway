package org.apache.isis.core.unittestsupport.jmocking;

public class CollaboratingUsingConstructorInjection {
    final Collaborator collaborator;

    public CollaboratingUsingConstructorInjection(final Collaborator collaborator) {
        this.collaborator = collaborator;
    }

    public void collaborateWithCollaborator() {
        collaborator.doOtherStuff();
    }
    
    public void dontCollaborateWithCollaborator() {
        
    }
    
}