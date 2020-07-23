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
    associateWith = "docxPropertyUsingMetaAnnotationButOverridden", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyFileAcceptVm_updateWithMetaAnnotationOverridden {

    private final PropertyFileAcceptVm propertyFileAcceptVm;

//tag::meta-annotation-overridden[]
    public PropertyFileAcceptVm act(
            @FileAcceptPdfMetaAnnotation                    // <.>
            @Parameter(
                fileAccept = ".docx"                        // <.>
            )
            @ParameterLayout(
                describedAs =
                    "@FileAcceptPdfMetaAnnotation " +
                    "@ParameterLayout(fileAccept = \".docx\")"
            )
            final Blob docxParameterUsingMetaAnnotationButOverridden) {
        propertyFileAcceptVm.setDocxPropertyUsingMetaAnnotationButOverridden(docxParameterUsingMetaAnnotationButOverridden);
        return propertyFileAcceptVm;
    }
//end::meta-annotation-overridden[]
    public Blob default0Act() {
        return propertyFileAcceptVm.getDocxPropertyUsingMetaAnnotationButOverridden();
    }

}
