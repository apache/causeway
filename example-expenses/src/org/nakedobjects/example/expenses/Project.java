/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    The authors can be contacted via www.nakedobjects.org (the
    registered address of Naked Objects Group is Kingsway House, 123 Goldworth
    Road, Woking GU21 1NR, UK).
*/

package org.nakedobjects.example.expenses;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.defaults.AbstractNakedObject;
import org.nakedobjects.object.defaults.Title;
import org.nakedobjects.object.defaults.value.TextString;


public class Project extends AbstractNakedObject {
    private final TextString name;
    private final InternalCollection teamMembers;
    private ProjectExpenses expenses;

    public Project() {
        teamMembers = createInternalCollection(Employee.class);
        name = new TextString();
    }

    public static String fieldOrder() {
        return "name, team members, expenses";
    }

    public void created() {
        expenses = (ProjectExpenses) createInstance(ProjectExpenses.class);
    }

    public void associateTeamMembers(Employee newTeamMember) {
    	newTeamMember.associateProjects(this);
    }

	 public void dissociateTeamMembers(Employee newTeamMember) {
        newTeamMember.dissociateProjects(this);
    }



    public InternalCollection getTeamMembers() {
        return teamMembers;
    }

    public Title title() {
        return new Title(name, "New Project");
    }

    public TextString getName() {
        return name;
    }

    public ProjectExpenses getExpenses() {
        resolve(expenses);

        return expenses;
    }

    public void setExpenses(ProjectExpenses expenses) {
        this.expenses = expenses;
        objectChanged();
    }
}
