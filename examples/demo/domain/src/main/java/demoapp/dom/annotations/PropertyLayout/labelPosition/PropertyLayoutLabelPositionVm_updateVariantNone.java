package demoapp.dom.annotations.PropertyLayout.labelPosition;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "propertyLabelPositionNone", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutLabelPositionVm_updateVariantNone {

    private final PropertyLayoutLabelPositionVm propertyLayoutLabelPositionVm;

//tag::annotation[]
    public PropertyLayoutLabelPositionVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                labelPosition = LabelPosition.NONE              // <.>
                , describedAs =
                    "@ParameterLayout(labelPosition = NONE)"
            )
            final String parameterLabelPositionNone) {
        propertyLayoutLabelPositionVm.setPropertyLabelPositionNone(parameterLabelPositionNone);
        return propertyLayoutLabelPositionVm;
    }
//end::annotation[]
    public String default0Act() {
        return propertyLayoutLabelPositionVm.getPropertyLabelPositionNone();
    }

}
