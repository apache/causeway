package demoapp.dom.annotations.PropertyLayout.named;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "propertyUsingEscapedMarkup", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutNamedVm_updateWithEscapedMarkup {

    private final PropertyLayoutNamedVm propertyLayoutNamedVm;

//tag::escaped-markup[]
    public PropertyLayoutNamedVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                named = "Named <b>but</b> <i>escaped</i>",          // <.>
                namedEscaped = true,                                // <.>
                describedAs =
                    "@ParameterLayout(named = \"...\", namedEscaped = true)"
            )
            final String newValue) {
        propertyLayoutNamedVm.setPropertyUsingEscapedMarkup(newValue);
        return propertyLayoutNamedVm;
    }
//end::escaped-markup[]
    public String default0Act() {
        return propertyLayoutNamedVm.getPropertyUsingEscapedMarkup();
    }

}
