package org.apache.causeway.viewer.graphql.viewer.test.domain.i18n;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.viewer.graphql.viewer.test.domain.calc.Month;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

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
