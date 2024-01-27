package org.apache.causeway.viewer.graphql.viewer.test.domain.calc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Named("university.calc.Calculator")
@DomainService(nature= NatureOfService.VIEW)
@Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class Calculator {

    @Action(semantics = SemanticsOf.SAFE)
    public byte addBytes(byte x, byte y) {
        return (byte)(x+y);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public int addByteWrappers(Byte x, @Parameter(optionality = Optionality.OPTIONAL) Byte y) {
        return y != null ? x+y : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public short addShorts(short x, short y) {
        return (short)(x+y);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Short addShortWrappers(Short x, @Parameter(optionality = Optionality.OPTIONAL) Short y) {
        return y != null ? (short)(x+y) : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public int addIntegers(int x, int y) {
        return x+y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public int addIntegerWrappers(Integer x, @Parameter(optionality = Optionality.OPTIONAL) Integer y) {
        return y != null ? x+y : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public double addDoubles(double x, double y) {
        return x+y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Double addDoubleWrappers(Double x, @Parameter(optionality = Optionality.OPTIONAL) Double y) {
        return y != null ? x+y : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public float addFloats(float x, float y) {
        return x+y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Float addFloatWrappers(Float x, @Parameter(optionality = Optionality.OPTIONAL) Float y) {
        return y != null ? (float)(x+y) : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public BigInteger addBigIntegers(BigInteger x, @Parameter(optionality = Optionality.OPTIONAL) BigInteger y) {
        return y != null ? x.add(y) : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public BigDecimal addBigDecimals(BigDecimal x, @Parameter(optionality = Optionality.OPTIONAL) BigDecimal y) {
        return y != null ? x.add(y) : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public LocalDate plusDays(LocalDate date, int numDays) {
        return date.plusDays(numDays);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public org.joda.time.LocalDate plusJodaDays(org.joda.time.LocalDate date, int numDays) {
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

    @Action(semantics = SemanticsOf.SAFE)
    public Month nextMonth(Month month) {
        return month.nextMonth();
    }

    @Action(semantics = SemanticsOf.SAFE)
    public String concat(String prefix, @Parameter(optionality = Optionality.OPTIONAL) String suffix) {
        return prefix + suffix;
    }

}
