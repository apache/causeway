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
    associateWith = "propertyLabelPositionRight", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutLabelPositionVm_updateVariantRight {

    private final PropertyLayoutLabelPositionVm propertyLayoutLabelPositionVm;

//tag::annotation[]
    public PropertyLayoutLabelPositionVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                labelPosition = LabelPosition.RIGHT              // <.>
                , describedAs =
                    "@ParameterLayout(labelPosition = RIGHT)"
            )
            final String parameterLabelPositionRight) {
        propertyLayoutLabelPositionVm.setPropertyLabelPositionRight(parameterLabelPositionRight);
        return propertyLayoutLabelPositionVm;
    }
//end::annotation[]
    public String default0Act() {
        return propertyLayoutLabelPositionVm.getPropertyLabelPositionRight();
    }

}
