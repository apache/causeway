package demoapp.dom._infra;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.core.commons.internal.collections._Maps;

import lombok.val;

@Service
@Named("demo.MarkupVariableResolverService")
public class MarkupVariableResolverService {

    private final Map<String, String> constants = _Maps.unmodifiable(
        "SOURCES_ISIS", "https://github.com/apache/isis/blob/master/core/applib/src/main/java",
        "SOURCES_DEMO", "https://github.com/apache/isis/tree/master/examples/demo/domain/src/main/java",
        "ISSUES_DEMO", "https://issues.apache.org/jira/",
        "ISIS_VERSION", "2.0.0-M3"
    );

    /**
     * For the given {@code input} replaces '${var-name}' with the variable's value.
     * @param input
     * @return
     */
    public String resolveVariables(String input) {
        val ref = new AtomicReference<String>(input);
        constants.forEach((k, v)->{
            ref.set(ref.get().replace(var(k), v));
        });
        return ref.get();
    }

    private String var(String name) {
        return String.format("${%s}", name);
    }

}
