package demoapp.dom.domain.progmodel.objects.embedded.jpa;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.SemanticsOf;

// tag::default-mixin[]
// ...
@Action(semantics = SemanticsOf.SAFE)
@ActionLayout(promptStyle = PromptStyle.INLINE_AS_IF_EDIT)
@RequiredArgsConstructor
public class ComplexNumberJpa_default {

    private final ComplexNumberJpa mixee;

    @MemberSupport
    public ComplexNumberJpa act(
            final double re,
            final double im
    ) {
        return ComplexNumberJpa.of(re, im);
    }

    @MemberSupport
    public double defaultRe() {
        return mixee.getRe();
    }

    @MemberSupport
    public double defaultIm() {
        return mixee.getIm();
    }
}
