package org.nakedobjects.example.movie.bom;

import org.nakedobjects.application.BusinessObjectContainer;
import org.nakedobjects.application.control.ActionAbout;

import java.util.Vector;


public class Movie {
    private BusinessObjectContainer container;
    private Person director;
    private String name;
    private final Vector roles = new Vector();

    public void addToRoles(Role role) {
        roles.addElement(role);
        setDirty();
    }

    public Person getDirector() {
        return director;
    }

    public String getName() {
        return name;
    }

    public Vector getRoles() {
        return roles;
    }

    public void removeFromRoles(Role role) {
        roles.removeElement(role);
        setDirty();
    }

    public void setDirector(Person director) {
        this.director = director;
        setDirty();
    }

    public void setName(String name) {
        this.name = name;
        setDirty();
    }

    private void setDirty() {
        container.objectChanged(this);
    }

    public String title() {
        return name;
    }
    
    public void setContainer(BusinessObjectContainer container) {
        this.container = container;
    }
    
    public static void aboutActionFindMovie(ActionAbout about, String name, Person director, Person actor) {
        about.setParameter(0, "Name");
        about.setParameter(1, "Director");
        about.setParameter(2, "Actor");
    }
    
    public static Movie[] actionFindMovie(String name, Person director, Person actor) {
 //       return MovieFinder.getFinder().findMovies(name, director, actor);
        return new Movie[0];
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */