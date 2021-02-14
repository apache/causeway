package org.apache.isis.applib.services.registry;

import java.util.Comparator;

import javax.annotation.Priority;

import org.apache.isis.commons.internal.reflection._Reflect;

import lombok.val;

/**
 * A comparator that orders objects according to their {@link Priority} annotation.
 *
 * @since 2.0  {@index}
 */
public class InstanceByPriorityComparator implements Comparator<Object> {

    private static final InstanceByPriorityComparator INSTANCE =
            new InstanceByPriorityComparator();

    public static InstanceByPriorityComparator instance() {
        return INSTANCE;
    }

    @Override
    public int compare(Object o1, Object o2) {

        if (o1 == null) {
            if (o2 == null) {
                return 0;
            } else {
                return -1; // o1 < o2
            }
        }
        if (o2 == null) {
            return 1; // o1 > o2
        }

        val prioAnnot1 = _Reflect.getAnnotation(o1.getClass(), Priority.class);
        val prioAnnot2 = _Reflect.getAnnotation(o2.getClass(), Priority.class);
        val prio1 = prioAnnot1 != null ? prioAnnot1.value() : 0;
        val prio2 = prioAnnot2 != null ? prioAnnot2.value() : 0;
        return Integer.compare(prio1, prio2);
    }

}
