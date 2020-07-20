package demoapp.dom.annotLayout.PropertyLayout.typicalLength;

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
public class PropertyLayoutTypicalLengthVm_updateWithParameterLayout {

    private final PropertyLayoutTypicalLengthVm propertyLayoutTypicalLengthVm;

//tag::annotation[]
    public PropertyLayoutTypicalLengthVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                typicalLength = 10                                 // <.>
                , describedAs =
                    "@ParameterLayout(typicalLength = 10)"
            )
            final String parameterUsingAnnotation) {
        propertyLayoutTypicalLengthVm.setPropertyUsingAnnotation(parameterUsingAnnotation);
        return propertyLayoutTypicalLengthVm;
    }
//end::annotation[]
    public String default0Act() {
        return propertyLayoutTypicalLengthVm.getPropertyUsingAnnotation();
    }

}
