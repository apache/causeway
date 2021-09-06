package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facets.ImperativeFacet.Intent;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.Value;
import lombok.val;

@Value(staticConstructor = "of")
public class ImperativeAspect {

    private final Can<Method> methods;
    private final Intent intent;

    public Intent getIntent(final Method method) {
        return intent;
    }

    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("methods",
                getMethods().stream()
                .map(Method::toString)
                .collect(Collectors.joining(", ")));
        getMethods().forEach(method->
            visitor.accept(
                    "intent." + method.getName(), getIntent(method)));
    }

    public static ImperativeAspect singleMethod(final Method method, final Intent checkIfDisabled) {
        return of(ImperativeFacet.singleMethod(method), checkIfDisabled);
    }

    public Object invokeSingleMethod(final ManagedObject domainObject) {
        val method = methods.getFirstOrFail();
        final Object returnValue = ManagedObjects.InvokeUtil.invoke(method, domainObject);
        return returnValue;
    }

}
