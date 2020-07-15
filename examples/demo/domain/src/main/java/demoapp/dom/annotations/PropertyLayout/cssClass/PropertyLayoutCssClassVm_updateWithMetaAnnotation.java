package demoapp.dom.annotations.PropertyLayout.cssClass;

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
public class PropertyLayoutCssClassVm_updateWithMetaAnnotation {

    private final PropertyLayoutCssClassVm propertyLayoutCssClassVm;

//tag::meta-annotation[]
    public PropertyLayoutCssClassVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @CssClassMetaAnnotation                            // <.>
            @ParameterLayout(
                describedAs = "@CssClassMetaAnnotation"
            )
            final String parameterUsingMetaAnnotation) {
        propertyLayoutCssClassVm.setPropertyUsingMetaAnnotation(parameterUsingMetaAnnotation);
        return propertyLayoutCssClassVm;
    }
//end::meta-annotation[]
    public String default0Act() {
        return propertyLayoutCssClassVm.getPropertyUsingMetaAnnotation();
    }

}
