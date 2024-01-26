package org.apache.causeway.viewer.graphql.viewer.test.domain;

import lombok.RequiredArgsConstructor;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

@Named("university.calc.Calculator")
@DomainService(nature= NatureOfService.VIEW)
@Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class Calculator {

    @Action(semantics = SemanticsOf.SAFE)
    public int add(int x, int y) {
        return x+y;
    }
}
