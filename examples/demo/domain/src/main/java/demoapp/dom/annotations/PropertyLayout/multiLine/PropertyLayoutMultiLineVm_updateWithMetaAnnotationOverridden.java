package demoapp.dom.annotations.PropertyLayout.multiLine;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom.annotations.PropertyLayout.named.NamedMetaAnnotation;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "propertyUsingMetaAnnotationButOverridden", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutMultiLineVm_updateWithMetaAnnotationOverridden {

    private final PropertyLayoutMultiLineVm propertyLayoutMultiLineVm;

//tag::meta-annotation-overridden[]
    public PropertyLayoutMultiLineVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @NamedMetaAnnotation                            // <.>
            @ParameterLayout(
                    multiLine = 3                           // <.>
            )
            final String newValue) {
        propertyLayoutMultiLineVm.setPropertyUsingMetaAnnotationButOverridden(newValue);
        return propertyLayoutMultiLineVm;
    }
//end::meta-annotation-overridden[]
    public String default0Act() {
        return propertyLayoutMultiLineVm.getPropertyUsingMetaAnnotationButOverridden();
    }

}
