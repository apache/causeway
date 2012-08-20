/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package dom.todo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.spi.PersistenceCapable;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.runtimes.dflt.objectstores.jdo.applib.Auditable;

@javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.DATASTORE)
@javax.jdo.annotations.Discriminator("TODO")
@javax.jdo.annotations.DatastoreIdentity(strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY)
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
        name="todo_notYetDone", language="JDOQL",  
        value="SELECT FROM dom.todo.ToDoItem WHERE ownedBy == :ownedBy && done == false"),
    @javax.jdo.annotations.Query(
        name="todo_similarTo", language="JDOQL",  
        value="SELECT FROM dom.todo.ToDoItem WHERE ownedBy == :ownedBy && category == :category")
})
@Version(strategy=VersionStrategy.VERSION_NUMBER, column="VERSION")
public class ToDoItem extends AbstractDomainObject implements Auditable {
    
    public static final List<String> CATEGORIES = Collections.unmodifiableList(Arrays.asList("Professional", "Domestic", "Other"));

    // {{ Description
    private String description;

    @Title
    @MemberOrder(sequence = "1")
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
    // }}

    // {{ Category
    private String category;

    @MemberOrder(sequence = "2")
    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }
    public List<String> choicesCategory() {
        return CATEGORIES;
    }
    // }}

    // {{ Done
    private boolean done;

    @Disabled
    @MemberOrder(sequence = "3")
    public boolean isDone() {
        return done;
    }

    public void setDone(final boolean done) {
        this.done = done;
    }

    // }}
    
    // {{ OwnedBy (property)
    private String ownedBy;

    @Hidden
    public String getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(final String ownedBy) {
        this.ownedBy = ownedBy;
    }
    // }}


    // {{ markAsDone
    @MemberOrder(sequence = "1")
    public ToDoItem markAsDone() {
        setDone(true);
        return this;
    }

    public String disableMarkAsDone() {
        return done ? "Already done" : null;
    }
    // }}

    // {{ markAsNotDone
    @MemberOrder(sequence = "2")
    public ToDoItem markAsNotDone() {
        setDone(false);
        return this;
    }

    public String disableMarkAsNotDone() {
        return !done ? "Not yet done" : null;
    }
    // }}


    // {{ Version (derived property)
    @Disabled
    @MemberOrder(sequence = "3")
    @Named("Version")
    public Long getVersionSequence() {
        if(!(this instanceof PersistenceCapable)) {
            return null;
        } 
        PersistenceCapable persistenceCapable = (PersistenceCapable) this;
        return (Long) JDOHelper.getVersion(persistenceCapable);
    }
    public boolean hideVersionSequence() {
        return !(this instanceof PersistenceCapable);
    }
    // }}




    // {{ injected: ToDoItems
    @SuppressWarnings("unused")
    private ToDoItems toDoItems;

    public void setToDoItems(final ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }
    // }}

}
