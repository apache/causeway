package org.apache.isis.extensions.bdd.testapp.claims.dom.employee;

import java.util.List;

import org.apache.isis.applib.annotation.Named;


@Named("Employees")
public interface EmployeeRepository {

    public List<Employee> allEmployees();

    public List<Employee> findEmployees(
    		@Named("Name") 
    		String name);
}
