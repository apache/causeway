package org.apache.isis.extensions.bdd.testapp.claims.service.employee;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.extensions.bdd.testapp.claims.dom.employee.Employee;
import org.apache.isis.extensions.bdd.testapp.claims.dom.employee.EmployeeRepository;


public class EmployeeRepositoryInMemory extends AbstractFactoryAndRepository implements EmployeeRepository {
	
	public EmployeeRepositoryInMemory() {
		// foo
	}

	// {{ Id, iconName
    public String getId() {
        return "claimants";
    }
    public String iconName() {
        return "EmployeeRepository";
    }
    // }}

    
    // {{ action: allEmployees
    public List<Employee> allEmployees() {
        return allInstances(Employee.class);
    }
    // }}

    
    // {{ action: findEmployees
    public List<Employee> findEmployees(String name) {
        return allMatches(Employee.class, name);
    }
    // }}
}
