package demoapp.dom.annotations.PropertyLayout.renderDay;

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
    associateWith = "endDateExclusive", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutRenderDayVm_updateEndDateWithParameterLayout {

    private final PropertyLayoutRenderDayVm propertyLayoutRenderDayVm;

//tag::annotation[]
    public PropertyLayoutRenderDayVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                renderDay = RenderDay.AS_DAY_BEFORE             // <.>
                , describedAs =
                    "@ParameterLayout(renderDay = AS_DAY_BEFORE)"
            )
            final LocalDate endDate) {
        propertyLayoutRenderDayVm.setEndDateExclusive(endDate);
        return propertyLayoutRenderDayVm;
    }
//end::annotation[]
    public LocalDate default0Act() {
        return propertyLayoutRenderDayVm.getEndDateExclusive();
    }

}
