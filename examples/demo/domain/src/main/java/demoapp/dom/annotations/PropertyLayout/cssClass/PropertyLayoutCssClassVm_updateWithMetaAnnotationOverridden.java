package demoapp.dom.annotations.PropertyLayout.cssClass;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom.annotations.PropertyLayout.multiLine.MultiLineMetaAnnotation;
import demoapp.dom.annotations.PropertyLayout.multiLine.PropertyLayoutMultiLineVm;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "propertyUsingMetaAnnotationButOverridden", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutCssClassVm_updateWithMetaAnnotationOverridden {

    private final PropertyLayoutCssClassVm propertyLayoutCssClassVm;

//tag::meta-annotation-overridden[]
    public PropertyLayoutCssClassVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @CssClassMetaAnnotation                             // <.>
            @ParameterLayout(
                cssClass = "blue"                               // <.>
                , describedAs =
                    "@CssClassMetaAnnotation @ParameterLayout(...)"
            )
            final String newValue) {
        propertyLayoutCssClassVm.setPropertyUsingMetaAnnotationButOverridden(newValue);
        return propertyLayoutCssClassVm;
    }
//end::meta-annotation-overridden[]
    public String default0Act() {
        return propertyLayoutCssClassVm.getPropertyUsingMetaAnnotationButOverridden();
    }

}
