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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.isis.commons.internal.collections._Lists;

import junit.framework.AssertionFailedError;

/**
 * @since 2.0 {@index}
 */
public final class PojoTester {

	public static class DatumFactory<T> {
		private Class<T> type;
		private T[] data;
		private int index;
		public DatumFactory() {
		}
		public DatumFactory(Class<T> type) {
			this.type = type;
		}
		@SafeVarargs
        public DatumFactory(Class<T> type, T... data) {
			this(type);
			this.data = data;
			index = data.length - 1;
		}
		public Class<T> getType() { return type; }
		public T getNext() {
			index = (index + 1) % data.length;
			return data[index];
		}
	}

   public static PojoTester create() {
        return new PojoTester();
   }

	private final Map<Class<?>, DatumFactory<?>> dataByType = new HashMap<>();
	private final AtomicInteger counter = new AtomicInteger();

	private PojoTester() {

        DatumFactory<Boolean> booleanDatumFactory = new DatumFactory<Boolean>(Boolean.class) {
			@Override
            public Boolean getNext() {
				return counter.getAndIncrement() == 0;
			}
		};
		dataByType.put(boolean.class, booleanDatumFactory);
		dataByType.put(Boolean.class, booleanDatumFactory);


		DatumFactory<Byte> byteDatumFactory = new DatumFactory<Byte>(Byte.class) {
			@Override
            public Byte getNext() {
				return (byte) counter.getAndIncrement();
			}
		};
		dataByType.put(byte.class, byteDatumFactory);
		dataByType.put(Byte.class, byteDatumFactory);


		DatumFactory<Short> shortDatumFactory = new DatumFactory<Short>(Short.class) {
			@Override
            public Short getNext() {
				return (short) counter.getAndIncrement();
			}
		};
		dataByType.put(short.class, shortDatumFactory);
		dataByType.put(Short.class, shortDatumFactory);


		DatumFactory<Character> charDatumFactory = new DatumFactory<Character>(Character.class) {
			@Override
            public Character getNext() {
				return (char) counter.getAndIncrement();
			}
		};
		dataByType.put(char.class, charDatumFactory);
		dataByType.put(Character.class, charDatumFactory);


		DatumFactory<Integer> intDatumFactory = new DatumFactory<Integer>(Integer.class) {
			@Override
            public Integer getNext() {
				return counter.getAndIncrement();
			}
		};
		dataByType.put(int.class, intDatumFactory);
		dataByType.put(Integer.class, intDatumFactory);


		DatumFactory<Long> longDatumFactory = new DatumFactory<Long>(Long.class) {
			@Override
            public Long getNext() {
				return (long) counter.getAndIncrement();
			}
		};
		dataByType.put(long.class, longDatumFactory);
		dataByType.put(Long.class, longDatumFactory);


		DatumFactory<Float> floatDatumFactory = new DatumFactory<Float>(Float.class) {
			@Override
            public Float getNext() {
				return new Float(counter.getAndIncrement());
			}
		};
		dataByType.put(float.class, floatDatumFactory);
		dataByType.put(Float.class, floatDatumFactory);


		DatumFactory<Double> doubleDatumFactory = new DatumFactory<Double>(Double.class) {
			@Override
            public Double getNext() {
				return new Double(counter.getAndIncrement());
			}
		};
		dataByType.put(double.class, doubleDatumFactory);
		dataByType.put(Double.class, doubleDatumFactory);

		dataByType.put(String.class, new DatumFactory<String>(String.class) {
			@Override
            public String getNext() {
				return "string" + counter.getAndIncrement();
			}
		});

		dataByType.put(BigDecimal.class, new DatumFactory<BigDecimal>(BigDecimal.class) {
			@Override
            public BigDecimal getNext() {
				return new BigDecimal(counter.getAndIncrement());
			}
		});

		dataByType.put(BigInteger.class, new DatumFactory<BigInteger>(BigInteger.class) {
			@Override
            public BigInteger getNext() {
				return BigInteger.valueOf(counter.getAndIncrement());
			}
		});

		dataByType.put(Date.class, new DatumFactory<Date>(Date.class) {
			@Override
            public Date getNext() {
				return new Date(counter.getAndIncrement());
			}
		});

		dataByType.put(Timestamp.class, new DatumFactory<Timestamp>(Timestamp.class) {
			@Override
            public Timestamp getNext() {
				return new Timestamp(counter.getAndIncrement());
			}
		});

		dataByType.put(Pattern.class, new DatumFactory<Pattern>(Pattern.class) {
			@Override
            public Pattern getNext() {
				return Pattern.compile("p" + counter.getAndIncrement());
			}
		});

		dataByType.put(File.class, new DatumFactory<File>(File.class) {
			@Override
            public File getNext() {
				return new File("file" + counter.getAndIncrement());
			}
		});

		DatumFactory<List<?>> listDatumFactory = new DatumFactory<List<?>>() {
			@Override
            public List<?> getNext() {
				final List<String> list = new ArrayList<>();
				list.add("element" + counter.getAndIncrement());
				list.add("element" + counter.getAndIncrement());
				list.add("element" + counter.getAndIncrement());
				return list;
			}
		};
		dataByType.put(Iterable.class, listDatumFactory);
		dataByType.put(Collection.class, listDatumFactory);
		dataByType.put(List.class, listDatumFactory);

		dataByType.put(Set.class, new DatumFactory<Set<?>>() {
			@Override
            public Set<?> getNext() {
				final Set<String> list = new HashSet<>();
				list.add("element" + counter.getAndIncrement());
				list.add("element" + counter.getAndIncrement());
				list.add("element" + counter.getAndIncrement());
				return list;
			}
		});
		dataByType.put(SortedSet.class, new DatumFactory<SortedSet<?>>() {
		    @Override
            public SortedSet<?> getNext() {
		        final SortedSet<String> list = new TreeSet<>();
		        list.add("element" + counter.getAndIncrement());
		        list.add("element" + counter.getAndIncrement());
		        list.add("element" + counter.getAndIncrement());
		        return list;
		    }
		});
		dataByType.put(byte[].class, new DatumFactory<byte[]>() {
		    @Override
            public byte[] getNext() {
		        return new byte[]{ (byte) counter.getAndIncrement() };
		    }
		});
		dataByType.put(char[].class, new DatumFactory<char[]>() {
		    @Override
            public char[] getNext() {
				return new char[]{ (char) counter.getAndIncrement() };
		    }
		});
	}

	public AtomicInteger getCounter() {
		return counter;
	}

	public <T> PojoTester usingData(Class<T> c, final T... data) {
		if (Enum.class.isAssignableFrom(c)) {
			throw new IllegalArgumentException("No need to provide test data for enums");
		}
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("Test data is mandatory");
		}
		return usingData(new DatumFactory<>(c, data));
	}

	public <T> PojoTester usingData(DatumFactory<T> factory) {
		dataByType.put(factory.getType(), factory);
		return this;
	}

	public void exercise(Object bean) {
		exercise(bean, FilterSet.excluding());
	}

	public void exercise(Object bean, FilterSet filterSet) {
		// an array that fills as each property is tested, allowing
		// subsequent properties to be tested against them
		final List<Method> gettersDone = new ArrayList<>();
		final List<TestException> problems = new ArrayList<>();

		final Map<String, Method> methods = getMethodsAsMap(bean);
		for (Entry<String, Method> e : methods.entrySet()) {
			final String methodName = e.getKey();
			if (methodName.startsWith("set")
					&& e.getValue().getParameterTypes().length == 1) {
				final char first = methodName.charAt(3);
				final String remainder = methodName.substring(4);
				final String property = Character.toLowerCase(first)
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
			final StringBuilder b = new StringBuilder();
			String newline = "";
			for (TestException te : problems) {
				b.append(newline).append(te.getMessage());
				newline = "\n";
				if (te.getCause() != null) {
					lastCause = te.getCause();
				}
			}
			final AssertionFailedError err = new AssertionFailedError(
					b.toString());
			if (lastCause != null) {
				err.initCause(lastCause);
			}
			throw err;
		}
	}

	private static Map<String, Method> getMethodsAsMap(Object bean) {
		final Map<String, Method> methodMap = new HashMap<>();
		for (Method m : bean.getClass().getMethods()) {
			methodMap.put(m.getName(), m);
		}
		return methodMap;
	}

	private void testOne(final Object bean, final Map<String, Method> methods,
			String property, List<Method> earlierGetters) throws TestException {
		final String setterName = getAccessor("set", property);
		for (Method setterMethod : methods.values()) {
			final Class<?>[] parameterTypes = setterMethod.getParameterTypes();
			if (setterMethod.getName().equals(setterName)
					&& parameterTypes.length == 1) {
				exercise(bean, property, methods, setterMethod,
						parameterTypes[0], earlierGetters);
				return;
			}
		}
		throw new TestException("No matching setter found for " + property
				+ ".");
	}

	private void exercise(final Object bean, String property,
			final Map<String, Method> methods, Method setterMethod,
			final Class<?> parameterType, List<Method> earlierGetters)
			throws AssertionFailedError, TestException {

		final String setterName = setterMethod.getName();
		DatumFactory<?> factory = dataByType.get(parameterType);
		if (factory == null) {
			// automatically populate for enums
			if (Enum.class.isAssignableFrom(parameterType)) {
				final Object[] testData = parameterType.getEnumConstants();
				factory = new DatumFactory<Object>(Object.class) {
					private int index = testData.length - 1;
					@Override
                    public Object getNext() {
						index = (index + 1) % testData.length;
						return testData[index];
					}
				};
				dataByType.put(parameterType, factory);
			} else {
				throw new TestException("No test data is available for "
						+ setterName + "( " + parameterType.getName() + " ).");
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
			final Method getterMethod = bean.getClass().getMethod(getterName);
			if (getterMethod.getReturnType().equals(void.class)) {
				throw new TestException(getterName + "(...) is void return.");
			}
			checkMethodVisibility(property, getterName, getterMethod);

			List<Object> earlierGetterOriginalValues = _Lists.newArrayList();
	        for (Method earlierGetter : earlierGetters) {
                final Object earlierValue = earlierGetter.invoke(bean);
                earlierGetterOriginalValues.add(earlierValue);
            }

			Object value = null;
			for (int i = 0; i < 3; i++) {
				value = factory.getNext();
				invokeSetterAndGetter(bean, property, setterMethod,
						getterMethod, value);

				// check hasn't changed value of any of the earlier getters
				int j=0;
	            for (Method earlierGetter : earlierGetters) {
	                final Object earlierGetterCurrentValue = earlierGetter.invoke(bean);
	                final Object earlierGetterOriginalValue = earlierGetterOriginalValues.get(j++);
                    if(!Objects.equals(earlierGetterOriginalValue, earlierGetterCurrentValue)) {
	                    throw new TestException(setterName
	                            + " interferes with " + earlierGetter.getName());
	                }
	            }
			}

			// finally store this getter to be tested against the next property
			earlierGetters.add(getterMethod);

		} catch (NoSuchMethodException e) {
            final TestException error = new TestException(property + ": "
                    + e.getMessage());
            error.initCause(e);
            throw error;
		} catch (Exception e) {
			final TestException error = new TestException(property + ": "
					+ e.getMessage());
			error.initCause(e);
			throw error;
		}
	}

	private static void checkMethodVisibility(String property,
			final String accessorName, final Method method)
			throws AssertionFailedError, TestException {
		if (!Modifier.isPublic(method.getModifiers())) {
			throw new TestException("Test failed for " + property + " because "
					+ accessorName + " is not publicly visible.");
		}
		if (!Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
			throw new TestException("Test failed for " + property + " because "
					+ accessorName
					+ " is declared in a class that is not publicly visible.");
		}
	}

	private static void invokeSetterAndGetter(final Object bean,
			String property, Method setterMethod, final Method getterMethod,
			Object t) throws IllegalAccessException, InvocationTargetException,
			AssertionFailedError, TestException {

		setterMethod.invoke(bean, t);
		final Object r = getterMethod.invoke(bean);
		if (!t.getClass().equals(r.getClass())) {
			throw new TestException("Test failed for " + property
					+ " because types do not match.");
		}

		if (!t.equals(r)) {
			throw new TestException("Test failed for " + property + " using "
					+ t.toString());
		}

		if (t instanceof Iterable<?>) {
			final Iterator<?> it = ((Iterable<?>) t).iterator();
			final Iterator<?> ir = ((Iterable<?>) r).iterator();
			while (it.hasNext() && ir.hasNext()) {
				final Object ti = it.next();
				final Object ri = ir.next();
				if (!ti.equals(ri)) {
					throw new TestException("Test failed for " + property
							+ " with iterator item " + ti.toString());
				}
			}
			if (it.hasNext() || ir.hasNext()) {
				throw new TestException("Test failed for " + property
						+ " because iteration lengths differ.");
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
			final FilterSet filterSet = new FilterSet(property);
			filterSet.include = true;
			return filterSet;
		}

		public static FilterSet excluding(String... property) {
			return new FilterSet(property);
		}
	}

}
