package demoapp.dom.annotations.PropertyLayout.named;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "propertyUsingAnnotation", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutNamedVm_updateWithParameterLayout {

    private final PropertyLayoutNamedVm propertyLayoutNamedVm;

//tag::annotation[]
    public PropertyLayoutNamedVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                named = "Named using @ParameterLayout"          // <.>
                , describedAs =
                    "@ParameterLayout(named = \"...\")"
            )
            final String parameterUsingAnnotation) {
        propertyLayoutNamedVm.setPropertyUsingAnnotation(parameterUsingAnnotation);
        return propertyLayoutNamedVm;
    }
//end::annotation[]
    public String default0Act() {
        return propertyLayoutNamedVm.getPropertyUsingAnnotation();
    }

}
