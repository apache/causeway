package org.nakedobjects.example.library;

import org.nakedobjects.container.exploration.Exploration;
import org.nakedobjects.object.exploration.AbstractExplorationFixture;
import org.nakedobjects.object.security.Role;
import org.nakedobjects.object.security.User;


public class LibraryAdmin extends Exploration {
    static String configFile;

    public static void main(String[] args) {
        configFile = args.length >= 1 ? args[0] : "nakedobjects.properties";
        new LibraryAdmin();
    }

protected void setUpFixtures() {
        addFixture(new AbstractExplorationFixture() {
            public void install() {
        		registerClass(User.class);
        		registerClass(Role.class);
            }
        });
   
    
        addFixture(new AbstractExplorationFixture() {
            public void install() {
        	if(needsInstances(Role.class)) {
        	    
    		/*
    		 	addInstance(Roles.ADMIN);
    			addInstance(Roles.LENDER);
    			addInstance(Roles.FILLER);
*/
        	    
    			User user = (User) createInstance(User.class);
    			user.getName().setValue("admin");
    			user.getRoles().add(Roles.ADMIN);
    			
    			user = (User) createInstance(User.class);
    			user.getName().setValue("guest");
    			user.getRoles().add(Roles.LENDER);
    		}
            }
        });
        
        }

}