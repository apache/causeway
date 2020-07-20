package demoapp.dom.annotLayout.PropertyLayout.describedAs;

import org.apache.isis.applib.annotation.Action;
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
public class PropertyLayoutDescribedAsVm_updateWithMetaAnnotationOverridden {

    private final PropertyLayoutDescribedAsVm propertyLayoutDescribedAsVm;

//tag::meta-annotation-overridden[]
    public PropertyLayoutDescribedAsVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @DescribedAsMetaAnnotation                                      // <.>
            @ParameterLayout(
                describedAs =                                               // <.>
                    "@DescribedAsMetaAnnotation @ParameterLayout(...)"
            )
            final String parameterUsingMetaAnnotationButOverridden) {
        propertyLayoutDescribedAsVm.setPropertyUsingMetaAnnotationButOverridden(parameterUsingMetaAnnotationButOverridden);
        return propertyLayoutDescribedAsVm;
    }
//end::meta-annotation-overridden[]
    public String default0Act() {
        return propertyLayoutDescribedAsVm.getPropertyUsingMetaAnnotationButOverridden();
    }

}
