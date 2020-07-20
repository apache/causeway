package demoapp.dom.annotLayout.PropertyLayout.labelPosition;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "propertyUsingMetaAnnotation", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutLabelPositionVm_updateWithMetaAnnotation {

    private final PropertyLayoutLabelPositionVm propertyLayoutLabelPositionVm;

//tag::meta-annotation[]
    public PropertyLayoutLabelPositionVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @LabelPositionTopMetaAnnotation                            // <.>
            @ParameterLayout(
                describedAs = "@LabelPositionTopMetaAnnotation"
            )
            final String parameterUsingMetaAnnotation) {
        propertyLayoutLabelPositionVm.setPropertyUsingMetaAnnotation(parameterUsingMetaAnnotation);
        return propertyLayoutLabelPositionVm;
    }
//end::meta-annotation[]
    public String default0Act() {
        return propertyLayoutLabelPositionVm.getPropertyUsingMetaAnnotation();
    }

}
