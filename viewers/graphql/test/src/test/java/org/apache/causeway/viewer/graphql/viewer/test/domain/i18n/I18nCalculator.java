package org.apache.causeway.viewer.graphql.viewer.test.domain.i18n;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Named("university.calc.I18nCalculator")
@DomainService(nature= NatureOfService.VIEW)
@Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class I18nCalculator {

    @Action(semantics = SemanticsOf.SAFE, asciiId = "concat")
    public String concät(
            @Parameter(asciiId = "a1")
            String ä1,
            @Parameter(asciiId = "a2")
            String ä2) {
        return ä1 + ä2;
    }

}
