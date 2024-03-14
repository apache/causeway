package org.apache.causeway.viewer.restfulobjects.test.domain.dom;

import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Named("university.dept.People")
@DomainService
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class People {

    private final StaffMemberRepository staffMemberRepository;
    private final DeptHeadRepository deptHeadRepository;

    @Action(semantics = SemanticsOf.SAFE)
    public Person findNamed(final String name) {
        return Optional.ofNullable((Person)staffMemberRepository.findByName(name))
                .orElse(deptHeadRepository.findByName(name));
    }

    @Action(semantics = SemanticsOf.SAFE)
    public String nameOf(final Person person) {
        return person.getName();
    }
}
