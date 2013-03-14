package org.apache.isis.core.unittestsupport.jmocking;

public class Collaborating {
    final Collaborator collaborator;

    public Collaborating(final Collaborator collaborator) {
        this.collaborator = collaborator;
    }

    public void collaborateWithCollaborator() {
        collaborator.doOtherStuff();
    }
    
    public void dontCollaborateWithCollaborator() {
        
    }
    
}