package demoapp.dom.domain.objects.progmodel.compositevalues;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

// tag::default-mixin[]
// ...
@Action(semantics = SemanticsOf.NON_IDEMPOTENT)
@ActionLayout(
        associateWith = "complexNumber",
        promptStyle = PromptStyle.DIALOG_SIDEBAR
)
@RequiredArgsConstructor
public class CompositeValueTypePage_subtractComplexNumber {

    private final CompositeValueTypePage mixee;

    @MemberSupport
    public CompositeValueTypePage act(
            final ComplexNumber current,
            final ComplexNumber other
    ) {
        mixee.setComplexNumber(current.subtract(other));
        return mixee;
    }

    @MemberSupport public ComplexNumber defaultCurrent() {
        return mixee.getComplexNumber();
    }

    @MemberSupport public String disableCurrent() {
        return "Number being added to";
    }


    @MemberSupport public ComplexNumber defaultOther() {
        return ComplexNumber.of(0,0);
    }

}
