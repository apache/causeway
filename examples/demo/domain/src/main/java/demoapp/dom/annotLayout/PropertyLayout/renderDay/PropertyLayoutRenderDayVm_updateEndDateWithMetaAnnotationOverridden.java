package demoapp.dom.annotLayout.PropertyLayout.renderDay;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RenderDay;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "endDateUsingMetaAnnotationButOverridden", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutRenderDayVm_updateEndDateWithMetaAnnotationOverridden {

    private final PropertyLayoutRenderDayVm propertyLayoutRenderDayVm;

//tag::meta-annotation-overridden[]
    public PropertyLayoutRenderDayVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @RenderDayMetaAnnotationStartDateInclusive          // <.>
            @ParameterLayout(
                renderDay = RenderDay.AS_DAY_BEFORE             // <.>
                , describedAs =
                    "@RenderDayMetaAnnotationStartDateInclusive " +
                    "@ParameterLayout(renderDay = AS_DAY_BEFORE)"
            )
            final LocalDate endDate) {
        propertyLayoutRenderDayVm.setEndDateUsingMetaAnnotationButOverridden(endDate);
        return propertyLayoutRenderDayVm;
    }
//end::meta-annotation-overridden[]
    public LocalDate default0Act() {
        return propertyLayoutRenderDayVm.getEndDateUsingMetaAnnotationButOverridden();
    }

}
