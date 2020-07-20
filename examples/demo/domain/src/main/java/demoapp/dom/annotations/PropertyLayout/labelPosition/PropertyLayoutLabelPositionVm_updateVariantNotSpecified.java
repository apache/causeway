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
    associateWith = "propertyLabelPositionNotSpecified", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutLabelPositionVm_updateVariantNotSpecified {

    private final PropertyLayoutLabelPositionVm propertyLayoutLabelPositionVm;

//tag::annotation[]
    public PropertyLayoutLabelPositionVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                labelPosition = LabelPosition.NOT_SPECIFIED         // <.>
                , describedAs =
                    "@ParameterLayout(labelPosition = NotSpecified)"
            )
            final String parameterLabelPositionNotSpecified) {
        propertyLayoutLabelPositionVm.setPropertyLabelPositionNotSpecified(parameterLabelPositionNotSpecified);
        return propertyLayoutLabelPositionVm;
    }
//end::annotation[]
    public String default0Act() {
        return propertyLayoutLabelPositionVm.getPropertyLabelPositionNotSpecified();
    }

}
