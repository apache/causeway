package demoapp.dom.domain.properties.ValueSemantics.percentage;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.core.metamodel.valuesemantics.BigDecimalValueSemantics;

//tag::class[]
@Component                                                          // <.>
@Named("demo.ValueSemanticsProviderForPercentage")
@Qualifier("percentage")                                            // <.>
@Priority(PriorityPrecedence.MIDPOINT)
public class ValueSemanticsProviderForPercentage
        extends BigDecimalValueSemantics {                          // <.>

        @Override
        public String htmlPresentation(
                final ValueSemanticsProvider.Context context,
                final BigDecimal value
        ) {
            return value.multiply(BigDecimal.valueOf(100))          // <.>
                        .setScale(2, RoundingMode.HALF_EVEN)
                    + "%";
        }
}
//end::class[]
