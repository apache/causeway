package org.apache.isis.applib.services.audit.spi;

public interface ChangingEntitiesListener {

    /**
     * Publish all changed entities at end of the transaction (during pre-commit phase).
     */
    void onEntitiesChanging(ChangingEntities changingEntities);      // <.>
}
