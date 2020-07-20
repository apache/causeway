package demoapp.dom.annotLayout.PropertyLayout.named;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "propertyUsingMarkup", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutNamedVm_updateWithMarkup {

    private final PropertyLayoutNamedVm propertyLayoutNamedVm;

//tag::markup[]
    public PropertyLayoutNamedVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                named = "Named <b>uses</b> <i>markup</i>",      // <.>
                namedEscaped = false,                           // <.>
                describedAs =
                    "@ParameterLayout(" +
                        "named = \"...\", namedEscaped = false)"
            )
            final String newValue) {
        propertyLayoutNamedVm.setPropertyUsingMarkup(newValue);
        return propertyLayoutNamedVm;
    }
//end::markup[]
    public String default0Act() {
        return propertyLayoutNamedVm.getPropertyUsingMarkup();
    }

}
