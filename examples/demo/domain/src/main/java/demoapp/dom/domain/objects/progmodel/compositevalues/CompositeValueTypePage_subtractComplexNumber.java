package demoapp.dom.domain.objects.progmodel.compositevalues;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

// tag::class[]
@Action(semantics = SemanticsOf.NON_IDEMPOTENT)
@ActionLayout(
        associateWith = "complexNumber",
        sequence = "2",
        promptStyle = PromptStyle.DIALOG_SIDEBAR
)
@RequiredArgsConstructor
public class CompositeValueTypePage_subtractComplexNumber {

    private final CompositeValueTypePage mixee;

    @MemberSupport public CompositeValueTypePage act(
            final ComplexNumber current,
            final ComplexNumber other
    ) {
        ComplexNumber result = current.subtract(other);
        mixee.setComplexNumber(result);
        return mixee;
    }
    @MemberSupport public ComplexNumber defaultCurrent() {
        return mixee.getComplexNumber();
    }
    @MemberSupport public String disableCurrent() {
        return "Number being subtracted from";
    }
    @MemberSupport public ComplexNumber defaultOther() {
        return ComplexNumber.of(0,0);
    }
}
// end::class[]
