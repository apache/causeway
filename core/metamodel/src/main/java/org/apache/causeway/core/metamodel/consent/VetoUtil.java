package org.apache.causeway.core.metamodel.consent;

import lombok.experimental.UtilityClass;

@UtilityClass
public class VetoUtil {

    public Consent.VetoReason withAdvisorAsDiagnostic(Consent.VetoReason vetoReason, Object advisor) {
        return vetoReason.withAdvisorAsDiagnostic(advisor);
    }

}
