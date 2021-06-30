/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.testing.unittestsupport.applib.dom.pojo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.opentest4j.AssertionFailedError;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.commons.internal.collections._Lists;

import lombok.Getter;

/**
 * Exercises the getters and setters of the provided pojo, ensuring that there are no side-effects.
 *
 * <p>
 *     As getters and setters are so simple, the intention of automating their testing is not to discover defects
 *     (though if there are unintentional side-effects, then these will be found).  Instead, the rationale of testing
 *     getters and setters is to increase code coverage.  Any substantial gap away from 100% would therefore due to
 *     significant functionality not having tests (as opposed to merely getters and setters not being tested).
 * </p>
 *
 * @since 2.0 {@index}
 */
public class PojoTester {

	/**
	 * Provides a set of data instances for the specified type.
	 *
	 * @see PojoTester#usingData(DatumFactory)
	 * @param <T>
	 */
	public interface DatumFactory<T> {
		/**
		 * The class that this factory returns values for.
		 *
		 * <p>
		 *     This factory will be used for any setters of this type.
		 * </p>
		 */
		Class<T> getType();

		/**
		 * Successively called to return a different instances.
		 *
		 * <p>
		 *     The method will be called exactly three times, and should return a different instance each time called.
		 * </p>
		 */
		T getNext();
	}

	static class DatumFactoryImpl<T> implements DatumFactory<T> {
		@Getter
		private final Class<T> type;
		private T[] dataArray;
		private List<T> dataList;
		private int index;
		DatumFactoryImpl() {
			this(null, Collections.emptyList());
		}
		@SafeVarargs
        DatumFactoryImpl(Class<T> type, T... data) {
			this.type = type;
			this.dataArray = data;
			index = data.length - 1;
		}
        DatumFactoryImpl(Class<T> type, List<T> data) {
			this.type = type;
			this.dataList = data;
			index = dataList.size();
		}
		public T getNext() {
			index = (index + 1) % dataArray.length;
			return dataList != null ? dataList.get(0) : dataArray[index];
		}
	}

	/**
	 * Factory method for the tester itself.
	 *
	 * <p>
	 *     This method is usually followed by several calls to {@link #usingData(DatumFactory)} or
	 *     {@link #usingData(Class, Object[])} (for all data types that are not built-in), and finally by
	 *     {@link #exercise(Object)} (or one of its overloads) which actually exercises the provided pojo.
	 * </p>
	 */
	public static PojoTester create() {
		return new PojoTester();
	}

	private final Map<Class<?>, DatumFactory<?>> dataByType = new HashMap<>();


	protected PojoTester() {

		final var booleanDatumFactory = new DatumFactoryImpl<>(Boolean.class) {
			@Override
			public Boolean getNext() {
				return counter.getAndIncrement() == 0;
			}
			private final AtomicInteger counter = new AtomicInteger();
		};
		usingData(boolean.class, booleanDatumFactory);
		usingData(Boolean.class, booleanDatumFactory);


		final var byteDatumFactory = new DatumFactoryImpl<>(Byte.class) {
			@Override
			public Byte getNext() {
				return (byte) counter.getAndIncrement();
			}
			private final AtomicInteger counter = new AtomicInteger();
		};
		usingData(byte.class, byteDatumFactory);
		usingData(Byte.class, byteDatumFactory);


		final var shortDatumFactory = new DatumFactoryImpl<>(Short.class) {
			@Override
			public Short getNext() {
				return (short) counter.getAndIncrement();
			}
			private final AtomicInteger counter = new AtomicInteger();
		};
		usingData(short.class, shortDatumFactory);
		usingData(Short.class, shortDatumFactory);


		final var charDatumFactory = new DatumFactoryImpl<>(Character.class) {
			@Override
			public Character getNext() {
				return (char) counter.getAndIncrement();
			}
			private final AtomicInteger counter = new AtomicInteger();
		};
		usingData(char.class, charDatumFactory);
		usingData(Character.class, charDatumFactory);


		final var intDatumFactory = new DatumFactoryImpl<>(Integer.class) {
			@Override
			public Integer getNext() {
				return counter.getAndIncrement();
			}
			private final AtomicInteger counter = new AtomicInteger();
		};
		usingData(int.class, intDatumFactory);
		usingData(Integer.class, intDatumFactory);


		final var longDatumFactory = new DatumFactoryImpl<>(Long.class) {
			@Override
			public Long getNext() {
				return (long) counter.getAndIncrement();
			}
			private final AtomicInteger counter = new AtomicInteger();
		};
		usingData(long.class, longDatumFactory);
		usingData(Long.class, longDatumFactory);


		final var floatDatumFactory = new DatumFactoryImpl<>(Float.class) {
			@Override
			public Float getNext() {
				return (float) counter.getAndIncrement();
			}
			private final AtomicInteger counter = new AtomicInteger();
		};
		usingData(float.class, floatDatumFactory);
		usingData(Float.class, floatDatumFactory);


		final var doubleDatumFactory = new DatumFactoryImpl<>(Double.class) {
			@Override
			public Double getNext() {
				return (double) counter.getAndIncrement();
			}
			private final AtomicInteger counter = new AtomicInteger();
		};
		usingData(double.class, doubleDatumFactory);
		usingData(Double.class, doubleDatumFactory);

		usingData(new DatumFactoryImpl<>(String.class) {
			@Override
			public String getNext() {
				return "string" + counter.getAndIncrement();
			}
			private final AtomicInteger counter = new AtomicInteger();
		});

		usingData(new DatumFactoryImpl<>(BigDecimal.class) {
			@Override
			public BigDecimal getNext() {
				return new BigDecimal(counter.getAndIncrement());
			}
			private final AtomicInteger counter = new AtomicInteger();
		});

		usingData(new DatumFactoryImpl<>(BigInteger.class) {
			@Override
			public BigInteger getNext() {
				return BigInteger.valueOf(counter.getAndIncrement());
			}
			private final AtomicInteger counter = new AtomicInteger();
		});

		usingData(new DatumFactoryImpl<>(Date.class) {
			@Override
			public Date getNext() {
				return new Date(counter.getAndIncrement());
			}
			private final AtomicInteger counter = new AtomicInteger();
		});

		usingData(new DatumFactoryImpl<>(Timestamp.class) {
			@Override
			public Timestamp getNext() {
				return new Timestamp(counter.getAndIncrement());
			}
			private final AtomicInteger counter = new AtomicInteger();
		});

		usingData(new DatumFactoryImpl<>(Pattern.class) {
			@Override
			public Pattern getNext() {
				return Pattern.compile("p" + counter.getAndIncrement());
			}
			private final AtomicInteger counter = new AtomicInteger();
		});

		usingData(new DatumFactoryImpl<>(File.class) {
			@Override
			public File getNext() {
				return new File("file" + counter.getAndIncrement());
			}
			private final AtomicInteger counter = new AtomicInteger();
		});

		final var listDatumFactory = new DatumFactoryImpl<List<?>>() {
			@Override
            public List<?> getNext() {
				final List<String> list = new ArrayList<>();
				list.add("element" + counter.getAndIncrement());
				list.add("element" + counter.getAndIncrement());
				list.add("element" + counter.getAndIncrement());
				return list;
			}
			private final AtomicInteger counter = new AtomicInteger();
		};
		usingData(Iterable.class, listDatumFactory);
		usingData(Collection.class, listDatumFactory);
		usingData(List.class, listDatumFactory);

		usingData(new DatumFactoryImpl<Set<?>>() {
			@Override
            public Set<?> getNext() {
				final Set<String> list = new HashSet<>();
				list.add("element" + counter.getAndIncrement());
				list.add("element" + counter.getAndIncrement());
				list.add("element" + counter.getAndIncrement());
				return list;
			}
			private final AtomicInteger counter = new AtomicInteger();
		});
		usingData(new DatumFactoryImpl<SortedSet<?>>() {
		    @Override
            public SortedSet<?> getNext() {
		        final SortedSet<String> list = new TreeSet<>();
		        list.add("element" + counter.getAndIncrement());
		        list.add("element" + counter.getAndIncrement());
		        list.add("element" + counter.getAndIncrement());
		        return list;
		    }
			private final AtomicInteger counter = new AtomicInteger();
		});
		usingData(new DatumFactoryImpl<byte[]>() {
		    @Override
            public byte[] getNext() {
		        return new byte[]{ (byte) counter.getAndIncrement() };
		    }
			private final AtomicInteger counter = new AtomicInteger();
		});
		usingData(new DatumFactoryImpl<char[]>() {
		    @Override
            public char[] getNext() {
				return new char[]{ (char) counter.getAndIncrement() };
		    }
			private final AtomicInteger counter = new AtomicInteger();
		});
		usingData(Blob.class,
					new Blob("foo", "application/pdf", new byte[]{1,2,3}),
					new Blob("bar", "application/docx", new byte[]{4,5}),
					new Blob("baz", "application/xlsx", new byte[]{7,8,9,0}));
		usingData(Clob.class,
					new Clob("foo", "text/html", "<html/>".toCharArray()),
					new Clob("bar", "text/plain", "hello world".toCharArray()),
					new Clob("baz", "text/ini", "foo=bar".toCharArray()));

		usingData(LocalTime.class,
					LocalTime.of(11, 15),
					LocalTime.of(12, 20),
					LocalTime.of(13, 30),
					LocalTime.of(14, 45));

		usingData(LocalDate.class,
					LocalDate.of(2012, 7, 19),
					LocalDate.of(2012, 7, 20),
					LocalDate.of(2012, 8, 19),
					LocalDate.of(2013, 7, 19));

		usingData(LocalDateTime.class,
					LocalDateTime.of(2012, 7, 19, 11, 15),
					LocalDateTime.of(2012, 7, 20, 12, 20),
					LocalDateTime.of(2012, 8, 19, 13, 30),
					LocalDateTime.of(2013, 7, 19, 14, 45));

		usingData(OffsetDateTime.class,
					OffsetDateTime.of(2012, 7, 19, 11, 15, 0, 0, ZoneOffset.UTC),
					OffsetDateTime.of(2012, 7, 20, 12, 20, 0, 0, ZoneOffset.UTC),
					OffsetDateTime.of(2012, 8, 19, 13, 30, 0, 0, ZoneOffset.UTC),
					OffsetDateTime.of(2013, 7, 19, 14, 45, 0, 0, ZoneOffset.UTC));

		usingData(org.joda.time.LocalDate.class,
					new org.joda.time.LocalDate(2012, 7, 19),
					new org.joda.time.LocalDate(2012, 7, 20),
					new org.joda.time.LocalDate(2012, 8, 19),
					new org.joda.time.LocalDate(2013, 7, 19)
		);

		usingData(org.joda.time.LocalTime.class,
					new org.joda.time.LocalTime(7, 19, 11, 15),
					new org.joda.time.LocalTime(7, 20, 12, 20),
					new org.joda.time.LocalTime(8, 19, 13, 30),
					new org.joda.time.LocalTime(7, 19, 14, 45)
		);

		usingData(org.joda.time.LocalDateTime.class,
					new org.joda.time.LocalDateTime(2012, 7, 19, 11, 15),
					new org.joda.time.LocalDateTime(2012, 7, 20, 12, 20),
					new org.joda.time.LocalDateTime(2012, 8, 19, 13, 30),
					new org.joda.time.LocalDateTime(2013, 7, 19, 14, 45)
		);

		usingData(DateTime.class,
					new org.joda.time.DateTime(2012, 7, 19, 11, 15),
					new org.joda.time.DateTime(2012, 7, 20, 12, 20),
					new org.joda.time.DateTime(2012, 8, 19, 13, 30),
					new org.joda.time.DateTime(2013, 7, 19, 14, 45)
		);
	}

	/**
	 * Provides a {@link DatumFactory} to the {@link PojoTester} in order to exercise any getter/setters of this
	 * type.
	 *
	 * <p>
	 *     The {@link PojoTester} already knows how to exercise primitives, strings, enums and a number of other
	 *     built-ins, so this is typically only necessary for custom value types.
	 * </p>
	 * @param factory
	 * @param <T>
	 * @return
	 */
	public <T> PojoTester usingData(DatumFactory<T> factory) {
		dataByType.put(factory.getType(), factory);
		return this;
	}
	<T> PojoTester usingData(final Class<?> type, DatumFactory<T> factory) {
		dataByType.put(type, factory);
		return this;
	}

	/**
	 * Convenience overload to provide a {@link DatumFactory} to the {@link PojoTester}, specifying both
	 * the type and a number of instances of that type.
	 *
	 * <p>
	 *     There should be at least two and ideally three different data instances.
	 * </p>
	 */
	public <T> PojoTester usingData(Class<T> c, final T... data) {
		if (Enum.class.isAssignableFrom(c)) {
			throw new IllegalArgumentException("No need to provide test data for enums");
		}
		if (data == null || data.length < 2) {
			throw new IllegalArgumentException("Test data is mandatory, at least two data items are required");
		}
		return usingData(new DatumFactoryImpl<>(c, data));
	}
	public <T> PojoTester usingData(Class<T> c, final List<T> data) {
		return usingData(new DatumFactoryImpl<>(c, data));
	}

	/**
	 * Convenience overload to provide a {@link DatumFactory} to the {@link PojoTester} for the specified
	 * compile time type, with the runtime type providing a no-arg constructor so that instances can be
	 * generated as required.
	 */
	@lombok.SneakyThrows
	public <T> PojoTester usingData(Class<T> compileTimeType, Class<? extends T> runtimeType) {
		final var declaredConstructor = runtimeType.getDeclaredConstructor();
		final T obj1 = declaredConstructor.newInstance();
		final T obj2 = declaredConstructor.newInstance();
		final T obj3 = declaredConstructor.newInstance();
		return usingData(compileTimeType, obj1, obj2, obj3);
	}

	/**
	 * Exercises all of the getters and setters of the provided bean, using the built-in {@link DatumFactory} and any
	 * additional configured through previous calls to {@link #usingData(Class, Object[])} (or its overloads).
	 */
	public void exercise(Object bean) {
		exercise(bean, FilterSet.excluding());
	}

	/**
	 * As for {@link #exercise(Object)}, however only exercising the properties as defined by the provided
	 * {@link FilterSet}, and omitting any others.
	 */
	public void exercise(Object bean, FilterSet filterSet) {
		// an array that fills as each property is tested, allowing
		// subsequent properties to be tested against them
		final List<Method> gettersDone = new ArrayList<>();
		final List<TestException> problems = new ArrayList<>();

		final var methods = getMethodsAsMap(bean);
		for (final var e : methods.entrySet()) {
			final var methodName = e.getKey();
			if (methodName.startsWith("set")
					&& e.getValue().getParameterTypes().length == 1) {
				final var first = methodName.charAt(3);
				final var remainder = methodName.substring(4);
				final var property = Character.toLowerCase(first)
						+ remainder;
				if (filterSet.shouldInclude(property)) {
					try {
						testOne(bean, methods, property, gettersDone);
					} catch (TestException te) {
						problems.add(te);
					}
				}
			}
		}
		handleExceptions(problems);
	}

	private static void handleExceptions(List<TestException> problems) {
		if (!problems.isEmpty()) {
			Throwable lastCause = null;
			final var b = new StringBuilder();
			var newline = "";
			for (var te : problems) {
				b.append(newline).append(te.getMessage());
				newline = "\n";
				if (te.getCause() != null) {
					lastCause = te.getCause();
				}
			}
			throw new AssertionFailedError(b.toString(), lastCause);
		}
	}

	private static Map<String, Method> getMethodsAsMap(Object bean) {
		final Map<String, Method> methodMap = new HashMap<>();
		for (final var m : bean.getClass().getMethods()) {
			methodMap.put(m.getName(), m);
		}
		return methodMap;
	}

	private void testOne(final Object bean, final Map<String, Method> methods,
			String property, List<Method> earlierGetters) throws TestException {
		final var setterName = getAccessor("set", property);
		for (final var setterMethod : methods.values()) {
			final var parameterTypes = setterMethod.getParameterTypes();
			if (setterMethod.getName().equals(setterName)
					&& parameterTypes.length == 1) {
				exercise(bean, property, methods, setterMethod,
						parameterTypes[0], earlierGetters);
				return;
			}
		}
		throw new TestException(String.format("No matching setter found for %s.", property));
	}

	private void exercise(final Object bean, String property,
			final Map<String, Method> methods, Method setterMethod,
			final Class<?> parameterType, List<Method> earlierGetters)
			throws AssertionFailedError, TestException {

		final var setterName = setterMethod.getName();
		var factory = dataByType.get(parameterType);
		if (factory == null) {
			// automatically populate for enums
			if (Enum.class.isAssignableFrom(parameterType)) {
				final var testData = parameterType.getEnumConstants();
				factory = new DatumFactoryImpl<>(Object.class) {
					private int index = testData.length - 1;

					@Override
					public Object getNext() {
						index = (index + 1) % testData.length;
						return testData[index];
					}
				};
				dataByType.put(parameterType, factory);
			} else {
				throw new TestException(String.format(
						"No test data is available for %s( %s ).",
						setterName, parameterType.getName()));
			}
		}

		checkMethodVisibility(property, setterName, setterMethod);

		String getterName;
		if (parameterType == boolean.class) {
			getterName = getAccessor("is", property);
			if (property.startsWith("Is") && !methods.containsKey(getterName)) {
				getterName = getAccessor("is", property.substring(2));
			}
		} else {
			getterName = getAccessor("get", property);
		}

		try {
			final var getterMethod = bean.getClass().getMethod(getterName);
			if (getterMethod.getReturnType().equals(void.class)) {
				throw new TestException(String.format("%s(...) is void return.", getterName));
			}
			checkMethodVisibility(property, getterName, getterMethod);

			List<Object> earlierGetterOriginalValues = _Lists.newArrayList();
	        for (var earlierGetter : earlierGetters) {
                final var earlierValue = earlierGetter.invoke(bean);
                earlierGetterOriginalValues.add(earlierValue);
            }

			Object value;
			for (var i = 0; i < 3; i++) {
				value = factory.getNext();
				invokeSetterAndGetter(bean, property, setterMethod,
						getterMethod, value);

				// check hasn't changed value of any of the earlier getters
				var j=0;
	            for (var earlierGetter : earlierGetters) {
	                final var earlierGetterCurrentValue = earlierGetter.invoke(bean);
	                final var earlierGetterOriginalValue = earlierGetterOriginalValues.get(j++);
                    if(!Objects.equals(earlierGetterOriginalValue, earlierGetterCurrentValue)) {
	                    throw new TestException(String.format(
	                    		"%s interferes with %s",
								setterName, earlierGetter.getName()));
	                }
	            }
			}

			// finally store this getter to be tested against the next property
			earlierGetters.add(getterMethod);

		} catch (Exception e) {
            final var error = new TestException(String.format("%s: %s", property, e.getMessage()));
            error.initCause(e);
            throw error;
		}
	}

	private static void checkMethodVisibility(String property,
			final String accessorName, final Method method)
			throws AssertionFailedError, TestException {
		if (!Modifier.isPublic(method.getModifiers())) {
			throw new TestException(String.format(
					"Test failed for %s because %s is not publicly visible.",
					property, accessorName));
		}
		if (!Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
			throw new TestException(String.format(
					"Test failed for %s because %s is declared in a class that is not publicly visible.",
					property, accessorName));
		}
	}

	private static void invokeSetterAndGetter(final Object bean,
			String property, Method setterMethod, final Method getterMethod,
			Object t) throws IllegalAccessException, InvocationTargetException,
			AssertionFailedError, TestException {

		setterMethod.invoke(bean, t);
		final var r = getterMethod.invoke(bean);
		if (!t.getClass().equals(r.getClass())) {
			throw new TestException("Test failed for " + property
					+ " because types do not match.");
		}

		if (!t.equals(r)) {
			throw new TestException(String.format("Test failed for %s using %s", property, t));
		}

		if (t instanceof Iterable<?>) {
			final var it = ((Iterable<?>) t).iterator();
			final var ir = ((Iterable<?>) r).iterator();
			while (it.hasNext() && ir.hasNext()) {
				final var ti = it.next();
				final var ri = ir.next();
				if (!ti.equals(ri)) {
					throw new TestException(String.format(
							"Test failed for %s with iterator item %s",
							property, ti));
				}
			}
			if (it.hasNext() || ir.hasNext()) {
				throw new TestException(String.format(
						"Test failed for %s because iteration lengths differ.", property));
			}
		}
	}

	private String getAccessor(String prefix, String property) {
		if (property.length() == 1) {
			return prefix + Character.toUpperCase(property.charAt(0));
		}
		return prefix + Character.toUpperCase(property.charAt(0))
				+ property.substring(1);
	}



	public static final class TestException extends Exception {

		private static final long serialVersionUID = 7870820619976334343L;

		public TestException(String message) {
			super(message);
		}

		public TestException(String message, Throwable t) {
			super(message, t);
		}
	}

	public static class FilterSet extends HashSet<String> {
		private static final long serialVersionUID = 1L;

		private boolean include = false;

		private FilterSet(String... string) {
			super.addAll(Arrays.asList(string));
		}

		private boolean shouldInclude(String x) {
			if (include) {
				return isEmpty() || contains(x);
			} else {
				return !contains(x);
			}
		}

		public static FilterSet includingOnly(String... property) {
			final var filterSet = new FilterSet(property);
			filterSet.include = true;
			return filterSet;
		}

		public static FilterSet excluding(String... property) {
			return new FilterSet(property);
		}
	}

}
