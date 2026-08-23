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
        sequence = "1",
        promptStyle = PromptStyle.DIALOG
)
@RequiredArgsConstructor
public class CompositeValueTypePage_addComplexNumber {

    private final CompositeValueTypePage mixee;

    @MemberSupport public CompositeValueTypePage act(
            final ComplexNumber current,                    // <.>
            final ComplexNumber other
    ) {
        ComplexNumber result = current.add(other);          // <.>
        mixee.setComplexNumber(result);
        return mixee;
    }
    @MemberSupport public ComplexNumber defaultCurrent() {  // <.>
        return mixee.getComplexNumber();
    }
    @MemberSupport public String disableCurrent() {         // <.>
        return "Number being added to";
    }
    @MemberSupport public ComplexNumber defaultOther() {
        return ComplexNumber.of(0,0);
    }
}
//end::class[]
