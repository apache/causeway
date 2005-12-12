package test.org.nakedobjects.objects.specification;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Action.Type;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.reflect.internal.NullSpecification;
import org.nakedobjects.object.value.adapter.StringAdapter;
import org.nakedobjects.utility.NakedObjectRuntimeException;

import test.org.nakedobjects.objects.bom.Movie;
import test.org.nakedobjects.objects.bom.Person;

public class MovieSpecification extends BasicSpecification {

    public MovieSpecification() {
        fields = new NakedObjectField[] {
                new MovieNameField(),
                new MovieDirectorField(),
           //     new MovieRolesField()
        };
    }
    
    public String getFullName() {
        return Movie.class.getName();
    }

    public Action getObjectAction(Type type, String name) {
        return null;
    }

    public Action getObjectAction(Type type, String name, NakedObjectSpecification[] parameters) {
        return null;
    }

    public Action[] getObjectActions(Type type) {
        return null;
    }

    public String getPluralName() {
        return "Movies";
    }

    public String getShortName() {
        return "movie";
    }

    public String getSingularName() {
        return "Movie";
    }

    public String getTitle(NakedObject naked) {
        return ((Movie) naked.getObject()).title();
    }

    public boolean isObject() {
        return true;
    }
}

class MovieDirectorField extends TestOneToOneAssociation {

    private Movie getMovie(NakedObject inObject) {
        return (Movie) inObject.getObject();
    }

    public void clearAssociation(NakedObject inObject, NakedObject associate) {
        if(getMovie(inObject).getDirector() != associate.getObject()) {
            throw new NakedObjectRuntimeException("clear value not set to " + getMovie(inObject).getDirector() + " but was " + associate.getObject());
        }
        getMovie(inObject).setDirector(null);
    }

    public void initAssociation(NakedObject inObject, NakedObject associate) {
        getMovie(inObject).setDirector(associate == null ? null : (Person) associate.getObject());
    }

    public void setAssociation(NakedObject inObject, NakedObject associate) {
        getMovie(inObject).setDirector((Person) associate.getObject());
    }

    public Consent isAssociationValid(NakedObject inObject, NakedObject associate) {
        return Allow.DEFAULT;
    }

    public Naked get(NakedObject fromObject) {
        Person director = getMovie(fromObject).getDirector();
        if(director == null) {
            return null;
        } else {
            return NakedObjects.getObjectLoader().getAdapterForElseCreateAdapterForTransient(director);
        }
    }

    public NakedObjectSpecification getSpecification() {
        return NakedObjects.getSpecificationLoader().loadSpecification(Person.class);
    }

    public String getId() {
        return "director";
    }
    
    public String getName() {
        return "Director";
    }
    
}

class MovieNameField extends TestValueField {
    public void clearValue(NakedObject inObject) {
        getMovie(inObject).setName("");
    }

    private Movie getMovie(NakedObject inObject) {
        return (Movie) inObject.getObject();
    }

    public void initValue(NakedObject inObject, Object value) {
        getMovie(inObject).setName((String) value);
    }

    public void setValue(NakedObject inObject, Object value) {
        getMovie(inObject).setName((String) value);
    }

    public Consent isValueValid(NakedObject inObject, NakedValue value) {
        return Allow.DEFAULT;
    }

    public Naked get(NakedObject fromObject) {
        return new StringAdapter(getMovie(fromObject).getName());
    }

    public NakedObjectSpecification getSpecification() {
        return new NullSpecification("java.lang.String");
    }

    public String getId() {
        return "name";
    }
    
    public String getName() {
        return "Name";
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