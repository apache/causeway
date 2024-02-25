package org.apache.causeway.viewer.graphql.viewer.test.domain.dept;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Named("university.dept.People")
@DomainService(
        nature= NatureOfService.VIEW)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class People {

    private final StaffMemberRepository staffMemberRepository;
    private final DeptHeadRepository deptHeadRepository;

    @Action(semantics = SemanticsOf.SAFE)
    public Person findNamed(String name) {
        return Optional.ofNullable((Person)staffMemberRepository.findByName(name))
                .orElse(deptHeadRepository.findByName(name));
    }

    @Action(semantics = SemanticsOf.SAFE)
    public String nameOf(Person person) {
        return person.getName();
    }
}
