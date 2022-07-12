package org.apache.isis.extensions.executionlog.applib.dom;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.isis.applib.exceptions.RecoverableException;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.isis.applib.services.repository.RepositoryService;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Provides supporting functionality for querying and persisting
 * {@link ExecutionLogEntry command} entities.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ExecutionLogEntryRepository<E extends ExecutionLogEntry> {

    public static class NotFoundException extends RecoverableException {
        private static final long serialVersionUID = 1L;
        @Getter
        private final UUID interactionId;
        public NotFoundException(final UUID interactionId) {
            super("Execution log entry not found");
            this.interactionId = interactionId;
        }
    }

    private final Class<E> executionLogEntryClass;

    /**
     * for testing only.
     */
    protected ExecutionLogEntryRepository(Class<E> executionLogEntryClass, Provider<RepositoryService> repositoryServiceProvider, FactoryService factoryService) {
        this.executionLogEntryClass = executionLogEntryClass;
        this.repositoryServiceProvider = repositoryServiceProvider;
        this.factoryService = factoryService;
    }

    public E createEntryAndPersist(final Execution execution) {
        E e = factoryService.detachedEntity(executionLogEntryClass);
        e.init(execution);
        persist(e);
        return e;
    }

    public List<E> findByInteractionId(UUID interactionId) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_INTERACTION_ID)
                        .withParameter("interactionId", interactionId));
    }

    public Optional<E> findByInteractionIdAndSequence(UUID interactionId, int sequence) {
        return repositoryService().firstMatch(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_INTERACTION_ID_AND_SEQUENCE)
                        .withParameter("interactionId", interactionId)
                        .withParameter("sequence", sequence)
        );
    }

    public List<E> find() {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND));
    }

    public List<E> findByTarget(Bookmark target) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET)
                        .withParameter("target", target));
    }

    public List<E> findByTargetAndTimestampAfter(Bookmark target, Timestamp timestamp) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_AFTER)
                        .withParameter("target", target)
                        .withParameter("timestamp", timestamp)
        );
    }

    public List<E> findByTargetAndTimestampBefore(Bookmark target, Timestamp timestamp) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BEFORE)
                        .withParameter("target", target)
                        .withParameter("timestamp", timestamp)
        );
    }

    public List<E> findByTargetAndTimestampBetween(Bookmark target, Timestamp timestampFrom, Timestamp timestampTo) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN)
                        .withParameter("target", target)
                        .withParameter("timestampFrom", timestampFrom)
                        .withParameter("timestampTo", timestampTo)
        );
    }

    public List<E> findByTimestampAfter(Timestamp timestamp) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_AFTER)
                        .withParameter("timestamp", timestamp)
        );
    }

    public List<E> findByTimestampBefore(Timestamp timestamp) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_BEFORE)
                        .withParameter("timestamp", timestamp)
        );
    }

    public List<E> findByTimestampBetween(Timestamp timestampFrom, Timestamp timestampTo) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_BETWEEN)
                        .withParameter("timestampFrom", timestampFrom)
                        .withParameter("timestampTo", timestampTo)
        );
    }

    public List<E> findRecentByUsername(String username) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_RECENT_BY_USERNAME)
                        .withParameter("username", username)
        );
    }

    public List<E> findByRecentByTarget(Bookmark target) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_RECENT_BY_TARGET)
                        .withParameter("target", target)
        );
    }

    private void persist(final E commandLogEntry) {
        repositoryService().persist(commandLogEntry);
    }

    private RepositoryService repositoryService() {
        return repositoryServiceProvider.get();
    }

    @Inject Provider<RepositoryService> repositoryServiceProvider;
    @Inject FactoryService factoryService;

}
