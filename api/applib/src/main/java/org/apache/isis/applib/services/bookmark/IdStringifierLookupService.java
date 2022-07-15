package org.apache.isis.applib.services.bookmark;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.commons.internal.base._Casts;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

/**
 * Convenience service that looks up (and caches) the {@link IdStringifier} available for a given value class, and
 * optionally the class of the owning entity.
 *
 * <p>
 *     Most
 * </p>
 */
@Service
@RequiredArgsConstructor
@Builder
public class IdStringifierLookupService {

    @Inject
    private final List<IdStringifier<?>> idStringifiers;
    private final Map<Class<?>, IdStringifier<?>> stringifierByClass = new ConcurrentHashMap<>();

    public <T> IdStringifier<T> lookupElseFail(Class<T> candidateValueClass) {
        val idStringifier = stringifierByClass.computeIfAbsent(candidateValueClass, aClass -> {
            for (val candidateStringifier : idStringifiers) {
                if (candidateStringifier.handles(candidateValueClass)) {
                    return candidateStringifier;
                }
            }
            return null;
        });
        return Optional.<IdStringifier<T>>ofNullable(_Casts.uncheckedCast(idStringifier))
                .orElseThrow(() -> new IllegalStateException(String.format("Could not locate an IdStringifier to handle '%s'", candidateValueClass)));
    }

    public <T> Optional<IdStringifier<T>> lookup(Class<T> candidateValueClass) {
        val idStringifier = stringifierByClass.computeIfAbsent(candidateValueClass, aClass -> {
            for (val candidateStringifier : idStringifiers) {
                if (candidateStringifier.handles(candidateValueClass)) {
                    return candidateStringifier;
                }
            }
            return null;
        });
        return Optional.ofNullable(_Casts.uncheckedCast(idStringifier));
    }

    @EqualsAndHashCode
    @ToString
    static class Key<T> {
        private final Class<T> valueClass;

        public Key(Class<T> valueClass) {
            this(valueClass, null);
        }
        public Key(Class<T> valueClass, Class<?> entityClassIAny) {
            this.valueClass = valueClass;
        }
    }
}
