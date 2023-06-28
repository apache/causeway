package org.apache.causeway.persistence.jdo.datanucleus.idstringifers;

import javax.annotation.Priority;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.datanucleus.identity.SCOID;

import org.springframework.stereotype.Component;

import lombok.NonNull;

@Component
@Priority(PriorityPrecedence.LATE + 100) // after the implementations of DatastoreId; for a custom impl.
public class IdStringifierForDnSCOID implements IdStringifier<SCOID> {

    @Override
    public Class<SCOID> getCorrespondingClass() {
        return SCOID.class;
    }

    @Override
    public String enstring(@NonNull SCOID value) {
        return value.getSCOClass();
    }

    @Override
    public SCOID destring(@NonNull Class<?> targetEntityClass, @NonNull String stringified) {
        return new SCOID(stringified);
    }
}
