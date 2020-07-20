package demoapp.dom.annotLayout.PropertyLayout.multiLine;

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
public class PropertyLayoutMultiLineVm_updateWithParameterLayout {

    private final PropertyLayoutMultiLineVm propertyLayoutMultiLineVm;

//tag::annotation[]
    public PropertyLayoutMultiLineVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                multiLine = 5                                 // <.>
                , describedAs =
                    "@ParameterLayout(multiLine = 5)"
            )
            final String parameterUsingAnnotation) {
        propertyLayoutMultiLineVm.setPropertyUsingAnnotation(parameterUsingAnnotation);
        return propertyLayoutMultiLineVm;
    }
//end::annotation[]
    public String default0Act() {
        return propertyLayoutMultiLineVm.getPropertyUsingAnnotation();
    }

}
