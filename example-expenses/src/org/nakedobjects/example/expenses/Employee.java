
package org.nakedobjects.example.expenses;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.valueholder.TextString;

import java.util.Vector;


public class Employee extends BaseObject {
    private final TextString firstName;
    private final TextString surname;
    private final Vector projects;
    private Account account;

    public Employee() {
        projects = new Vector();
        firstName = new TextString();
        surname = new TextString();
    }

    public static String fieldOrder() {
        return "First name, surname, projects, account";
    }

    public void created() {
        account =new Account();
    }

    public void addToProjects(Project project) {
        projects.add(project);
        project.getTeamMembers().addElement(this);
    }

    public void removeFromProjects(Project project) {
        projects.remove(project);
        project.getTeamMembers().removeElement(this);
    }

    public Vector getProjects() {
        return projects;
    }

    public Title title() {
        if (surname.isEmpty() && firstName.isEmpty()) {
            return new Title("New User");
        }

        return surname.title().append(",", firstName.titleString());
    }

    public String toString() {
        return surname + super.toString();
    }

    public TextString getFirstName() {
        return firstName;
    }

    public Account getAccount() {
        resolve(account);

        return account;
    }

    public void setAccount(Account newAccount) {
        account = newAccount;
        objectChanged();
    }

    public TextString getSurname() {
        return surname;
    }
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
