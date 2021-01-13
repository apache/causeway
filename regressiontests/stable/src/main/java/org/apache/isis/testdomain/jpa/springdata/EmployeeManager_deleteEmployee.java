package org.apache.isis.testdomain.jpa.springdata;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;

import lombok.RequiredArgsConstructor;

@Action(associateWith = "allEmployees")
@RequiredArgsConstructor
public class EmployeeManager_deleteEmployee {

    @Inject private EmployeeRepository employeeRepo;
    
    private final EmployeeManager holder;
    
    public EmployeeManager act(List<Employee> employeesToRemove) {
        employeesToRemove.forEach(employeeRepo::delete);
        return holder;
    }
    
}
