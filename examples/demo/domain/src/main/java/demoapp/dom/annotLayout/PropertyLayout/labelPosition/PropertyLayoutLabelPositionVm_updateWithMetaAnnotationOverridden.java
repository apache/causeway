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
    associateWith = "propertyUsingMetaAnnotationButOverridden", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutLabelPositionVm_updateWithMetaAnnotationOverridden {

    private final PropertyLayoutLabelPositionVm propertyLayoutLabelPositionVm;

//tag::meta-annotation-overridden[]
    public PropertyLayoutLabelPositionVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @LabelPositionTopMetaAnnotation                             // <.>
            @ParameterLayout(
                labelPosition = LabelPosition.LEFT                      // <.>
                , describedAs =
                    "@LabelPositionTopMetaAnnotation @ParameterLayout(...)"
            )
            final String parameterUsingMetaAnnotationButOverridden) {
        propertyLayoutLabelPositionVm.setPropertyUsingMetaAnnotationButOverridden(parameterUsingMetaAnnotationButOverridden);
        return propertyLayoutLabelPositionVm;
    }
//end::meta-annotation-overridden[]
    public String default0Act() {
        return propertyLayoutLabelPositionVm.getPropertyUsingMetaAnnotationButOverridden();
    }

}
