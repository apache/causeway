package org.apache.causeway.viewer.graphql.viewer.test.domain.calc;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

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
    public int addIntegers(int x, int y) {
        return x+y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public double addDoubles(double x, double y) {
        return x+y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public float addFloats(float x, float y) {
        return x+y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public BigInteger addBigIntegers(BigInteger x, BigInteger y) {
        return x.add(y);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public BigDecimal addBigDecimals(BigDecimal x, BigDecimal y) {
        return x.add(y);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public LocalDate plusDays(LocalDate date, int numDays) {
        return date.plusDays(numDays);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public boolean and(boolean x, boolean y) {
        return x & y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public boolean or(boolean x, boolean y) {
        return x | y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public boolean not(boolean x) {
        return !x;
    }

}
