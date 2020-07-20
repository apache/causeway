package demoapp.dom.annotLayout.PropertyLayout.labelPosition;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.LabelPosition;
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
public class PropertyLayoutLabelPositionVm_updateWithParameterLayout {

    private final PropertyLayoutLabelPositionVm propertyLayoutLabelPositionVm;

//tag::annotation[]
    public PropertyLayoutLabelPositionVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                labelPosition = LabelPosition.TOP           // <.>
                , describedAs =
                    "@ParameterLayout(labelPosition = TOP)"
            )
            final String parameterUsingAnnotation) {
        propertyLayoutLabelPositionVm.setPropertyUsingAnnotation(parameterUsingAnnotation);
        return propertyLayoutLabelPositionVm;
    }
//end::annotation[]
    public String default0Act() {
        return propertyLayoutLabelPositionVm.getPropertyUsingAnnotation();
    }

}
