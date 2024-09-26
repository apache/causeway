package org.apache.causeway.persistence.querydsl.applib.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;

import lombok.experimental.UtilityClass;

@UtilityClass
public class QueryDslUtil {
    public final static Pattern REGEX_PATTERN = Pattern.compile("\\(\\?i\\)");//Pattern to recognize StringUtils#wildcardToCaseInsensitiveRegexAlways conversion
    public static final OrderSpecifier<Comparable> ID_ORDER_SPECIFIER = new OrderSpecifier<>(Order.ASC, constant("id"));

    /**
     * Creates {@link BooleanOperation} where the arguments use the operator 'startsWith'.
     * Equivalent to SQL clause '<argument>.startsWith(<path>)'.
     * This has a different outcome compared to the '<path>.startsWith(<argument>)'
     */
    public static <T> BooleanOperation startsWith(final T argument, final Path<T> path) {
        return Expressions.predicate(Ops.STARTS_WITH, constant(argument), path);
    }

    /**
     * Creates {@link BooleanOperation} where the arguments is checked for null.
     * Equivalent with SQL clause '<path> = <argument>' or '<path> IS NULL'.
     */
    public static <T> BooleanOperation eqOrNull(final Path<T> path, final T argument) {
        return Optional.ofNullable(argument)
                .map(a -> Expressions.predicate(Ops.EQ, path, constant(a)))
                .orElse(Expressions.predicate(Ops.IS_NULL, path));
    }

    public static BooleanExpression searchAndReplace(final StringPath stringPath, final String searchPhrase, final boolean ignoreCase, final boolean always) {
        return search(stringPath, replaceWildcards(searchPhrase, always), ignoreCase);
    }

    public static BooleanExpression search(final StringPath stringPath, final String searchPhrase, final boolean ignoreCase) {
        if (REGEX_PATTERN.matcher(searchPhrase).find()) {
            return stringPath.matches(searchPhrase);
        }
        if (ignoreCase) {
            return stringPath.likeIgnoreCase(searchPhrase);
        }
        return stringPath.like(searchPhrase);
    }

    public static <T> Expression<T> constant(final T argument) {
        if (argument == null) return null;
        return Expressions.constant(argument);
    }

    public static Predicate and(List<? extends Predicate> predicates) {
        return and(predicates.toArray(new Predicate[0]));
    }

    public static Predicate and(Predicate... predicates) {
        if (predicates.length == 1) {
            return predicates[0];
        } else if (predicates.length > 2) {
            Predicate[] predicate = new Predicate[]{and(Arrays.copyOf(predicates, 2))};
            Predicate[] remainder = Arrays.copyOfRange(predicates, 2, predicates.length);
            if (remainder.length == 1) {
                return and(ArrayUtils.addAll(predicate, remainder[0]));
            } else if (remainder.length > 2) {
                return and(ArrayUtils.addAll(predicate, remainder));
            }
            return and(ArrayUtils.addAll(predicate, and(remainder)));
        }
        return Expressions.predicate(Ops.AND, Arrays.stream(predicates).map(ExpressionUtils::extract).toArray(Expression[]::new));
    }

    public static Predicate or(List<? extends Predicate> predicates) {
        return or(predicates.toArray(new Predicate[0]));
    }

    public static Predicate or(Predicate... predicates) {
        if (predicates.length == 1) {
            return predicates[0];
        } else if (predicates.length > 2) {
            Predicate[] predicate = new Predicate[]{or(Arrays.copyOf(predicates, 2))};
            Predicate[] remainder = Arrays.copyOfRange(predicates, 2, predicates.length);
            if (remainder.length == 1) {
                return or(ArrayUtils.addAll(predicate, remainder[0]));
            } else if (remainder.length > 2) {
                return or(ArrayUtils.addAll(predicate, remainder));
            }
            return or(ArrayUtils.addAll(predicate, or(remainder)));
        }
        return Expressions.predicate(Ops.OR, Arrays.stream(predicates).map(ExpressionUtils::extract).toArray(Expression[]::new));
    }

    public static String replaceWildcards(final String search, final boolean always) {
        if (REGEX_PATTERN.matcher(search).find()) {
            // Don't replace anything when regex is given
            return search;
        }
        String result = search.replace("*", "%").replace("?", "_");
        if (always && !result.contains("%") && !result.contains("_"))
            result = "%" + result + "%";
        return result;
    }

    public static <T> List<T> newList(T... objs) {
        return newArrayList(objs);
    }

    static <T> ArrayList<T> newArrayList(T... objs) {
        ArrayList<T> result = new ArrayList();
        Collections.addAll(result, objs);
        return result;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static <T> Class<T> getTypeParameter(Class<?> parameterizedType, int index){
        if(parameterizedType==null) return null;

        ParameterizedType pType= (ParameterizedType) parameterizedType.getGenericSuperclass();
        if(pType==null) return null;

        Type[] types= pType.getActualTypeArguments();
        if(types==null || types.length<=index || types[index] instanceof ParameterizedType) return null;

        return (Class<T>) types[index];
    }

}