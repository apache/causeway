package org.nakedobjects.example.movie.exploration;

import org.nakedobjects.application.system.ExplorationClock;
import org.nakedobjects.application.value.Date;
import org.nakedobjects.example.movie.bom.Movie;
import org.nakedobjects.example.movie.bom.Person;
import org.nakedobjects.example.movie.bom.Role;
import org.nakedobjects.example.movie.objectstore.MovieMapper;
import org.nakedobjects.example.movie.objectstore.PersonMapper;
import org.nakedobjects.example.movie.objectstore.RoleMapper;
import org.nakedobjects.example.movie.objectstore.SqlObjectStore;
import org.nakedobjects.example.movie.objectstore.SqlOidGenerator;
import org.nakedobjects.object.persistence.DefaultPersistAlgorithm;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.objectstore.ObjectStorePersistor;
import org.nakedobjects.system.NakedObjectsSystem;


public class MovieStandalone {
    private NakedObjectsSystem system;

    public MovieStandalone() {
        system = new NakedObjectsSystem() {
            protected ObjectStorePersistor createObjectPersistor() {
                SqlObjectStore objectStore = new SqlObjectStore();
                OidGenerator oidGenerator = new SqlOidGenerator();
                objectStore.addMapper(Movie.class, new MovieMapper());
                objectStore.addMapper(Person.class, new PersonMapper());
                objectStore.addMapper(Role.class, new RoleMapper());

                DefaultPersistAlgorithm persistAlgorithm = new DefaultPersistAlgorithm();
                persistAlgorithm.setOidGenerator(oidGenerator);

                ObjectStorePersistor objectManager = new ObjectStorePersistor();
                objectManager.setObjectStore(objectStore);
                objectManager.setPersistAlgorithm(persistAlgorithm);
                return objectManager;
            }
        };
        system.init();
    }
    
    public static void main(String[] args) {
        MovieStandalone e = new MovieStandalone();
        
        Date.setClock(new ExplorationClock());
        
        e.system.displayUserInterface( new String[] {Movie.class.getName(), Person.class.getName()});
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2004 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */