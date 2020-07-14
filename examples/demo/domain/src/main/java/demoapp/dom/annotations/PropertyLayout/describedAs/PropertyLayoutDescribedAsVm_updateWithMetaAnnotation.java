package demoapp.dom.annotations.PropertyLayout.describedAs;

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
public class PropertyLayoutDescribedAsVm_updateWithMetaAnnotation {

    private final PropertyLayoutDescribedAsVm propertyLayoutDescribedAsVm;

//tag::meta-annotation[]
    public PropertyLayoutDescribedAsVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @DescribedAsMetaAnnotation                            // <.>
            @ParameterLayout(
                describedAs = "@DescribedAsMetaAnnotation"
            )
            final String newValue) {
        propertyLayoutDescribedAsVm.setPropertyUsingMetaAnnotation(newValue);
        return propertyLayoutDescribedAsVm;
    }
//end::meta-annotation[]
    public String default0Act() {
        return propertyLayoutDescribedAsVm.getPropertyUsingMetaAnnotation();
    }

}
