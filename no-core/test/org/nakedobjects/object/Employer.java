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

package org.nakedobjects.object;


import org.nakedobjects.object.defaults.AbstractNakedObject;
import org.nakedobjects.object.defaults.Title;
import org.nakedobjects.object.defaults.value.TextString;
import org.nakedobjects.object.defaults.value.WholeNumber;


public abstract class Employer extends AbstractNakedObject {
    private static final long serialVersionUID = 3L;

    public Employee actionCheck(Employee employee) {
        if (getEmployees().contains(employee)) {
        	return employee;
        }
        else {
        	return null;
        }
    }

    public void actionMakeLimited() {
        getCompanyName().setValue(getCompanyName().title().toString() + " Ltd.");
    }

    public Employee actionNewEmployee() {
        Employee employee = (Employee) createInstance(Employee.class);

        addEmployees(employee);
        return employee;
    }

    public void addEmployees(Employee newEmployee) {
        newEmployee.setEmployer(this);
        getEmployees().add(newEmployee);
    }

    public WholeNumber deriveNumberOfEmployees() {
        return new WholeNumber(getEmployees().size());
    }

    public abstract Employee getCEO();

    public abstract TextString getCompanyName();

    public abstract InternalCollection getEmployees();

    public abstract NakedCollection getIntraCompanyTeam();

    public void removeEmployees(Employee removeEmployee) {
        removeEmployee.setEmployer(null);
        getEmployees().remove(removeEmployee);
    }

    public abstract void setCEO(Employee ceo);

    public abstract void setIntraCompanyTeam(NakedCollection newIntraCompanyTeam);

    public Title title() {
        return getCompanyName().title();
    }
}
