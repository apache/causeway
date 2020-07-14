package demoapp.dom.annotations.PropertyLayout.multiLine;

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
public class PropertyLayoutMultiLineVm_updateWithMetaAnnotation {

    private final PropertyLayoutMultiLineVm propertyLayoutMultiLineVm;

//tag::meta-annotation[]
    public PropertyLayoutMultiLineVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @MultiLineMetaAnnotation                            // <.>
            @ParameterLayout(
                describedAs = "@MultiLineMetaAnnotation"
            )
            final String newValue) {
        propertyLayoutMultiLineVm.setPropertyUsingMetaAnnotation(newValue);
        return propertyLayoutMultiLineVm;
    }
//end::meta-annotation[]
    public String default0Act() {
        return propertyLayoutMultiLineVm.getPropertyUsingMetaAnnotation();
    }

}
