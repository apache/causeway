package demoapp.dom.types;

import java.util.stream.Stream;

public interface Samples<T> {

    Stream<T> stream();

    default T single() {
        return stream().findFirst().orElse(null);
    }
}
