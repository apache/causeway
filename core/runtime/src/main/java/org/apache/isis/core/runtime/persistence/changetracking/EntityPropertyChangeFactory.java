package org.apache.isis.core.runtime.persistence.changetracking;

import java.util.UUID;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "of")
class EntityPropertyChangeFactory {
    
    public static EntityPropertyChange createEntityPropertyChange(
            final java.sql.Timestamp timestamp,
            final String user,
            final TransactionId txId,
            final PropertyChangeRecord propertyChangeRecord) {
        
        val adapterAndProperty = propertyChangeRecord.getAdapterAndProperty();
        val spec = adapterAndProperty.getAdapter().getSpecification();

        final Bookmark target = adapterAndProperty.getBookmark();
        final String propertyId = adapterAndProperty.getPropertyId();
        final String memberId = adapterAndProperty.getMemberId();

        final PreAndPostValues papv = propertyChangeRecord.getPreAndPostValues();
        final String preValue = papv.getPreString();
        final String postValue = papv.getPostString();

        final String targetClass = CommandUtil.targetClassNameFor(spec);

        final UUID transactionId = txId.getUniqueId();
        final int sequence = txId.getSequence();

        return EntityPropertyChange.of(
                transactionId, sequence, targetClass, target, 
                memberId, propertyId, preValue, postValue, user, timestamp);
    }
}
