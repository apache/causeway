package demoapp.dom.annotDomain.Property.maxLength;

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
public class PropertyMaxLengthVm_updateWithParameterLayout {

    private final PropertyMaxLengthVm propertyMaxLengthVm;

//tag::annotation[]
    public PropertyMaxLengthVm act(
            @Parameter(
                maxLength = 10                                 // <.>
            )
            @ParameterLayout(
                describedAs =
                    "@Parameter(maxLength = 10)"
            )
            final String parameterUsingAnnotation) {
        propertyMaxLengthVm.setPropertyUsingAnnotation(parameterUsingAnnotation);
        return propertyMaxLengthVm;
    }
//end::annotation[]
    public String default0Act() {
        return propertyMaxLengthVm.getPropertyUsingAnnotation();
    }

}
