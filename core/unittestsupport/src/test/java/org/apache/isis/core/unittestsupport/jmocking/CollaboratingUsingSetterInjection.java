package org.apache.isis.core.unittestsupport.jmocking;

public class CollaboratingUsingSetterInjection {
    Collaborator collaborator;

    public CollaboratingUsingSetterInjection() {
    }

    public void setCollaborator(Collaborator collaborator) {
        this.collaborator = collaborator;
    }
    
    public void collaborateWithCollaborator() {
        collaborator.doOtherStuff();
    }
    
    public void dontCollaborateWithCollaborator() {
        
    }
    
}