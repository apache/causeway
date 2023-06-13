package demoapp.dom.progmodel.customvaluetypes.compositevalues;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(semantics = SemanticsOf.NON_IDEMPOTENT)
@ActionLayout(
        associateWith = "complexNumber",
        cssClassFa = "fa-minus-square",
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
//end::class[]
