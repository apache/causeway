package demoapp.dom.annotDomain.Property.fileAccept;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Blob;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "propertyUsingMetaAnnotationButOverridden", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyFileAcceptVm_updateWithMetaAnnotationOverridden {

    private final PropertyFileAcceptVm propertyFileAcceptVm;

//tag::meta-annotation-overridden[]
    public PropertyFileAcceptVm act(
            @Parameter(
                maxLength = 3                                   // <.>
                , optionality = Optionality.OPTIONAL
            )
            @FileAcceptPdfMetaAnnotation                            // <.>
            @ParameterLayout(
                describedAs =
                    "@FileAcceptPdfMetaAnnotation @ParameterLayout(...)"
            )
            final Blob parameterUsingMetaAnnotationButOverridden) {
        propertyFileAcceptVm.setPropertyUsingMetaAnnotationButOverridden(parameterUsingMetaAnnotationButOverridden);
        return propertyFileAcceptVm;
    }
//end::meta-annotation-overridden[]
    public Blob default0Act() {
        return propertyFileAcceptVm.getPropertyUsingMetaAnnotationButOverridden();
    }

}
