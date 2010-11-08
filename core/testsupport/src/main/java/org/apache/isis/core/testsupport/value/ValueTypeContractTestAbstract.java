package org.apache.isis.core.testsupport.value;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Contract test for value types ({@link #equals(Object)} and {@link #hashCode()}).
 */
public abstract class ValueTypeContractTestAbstract<T> {
	
	@Before
	public void setUp() throws Exception {
		assertSizeAtLeast(getObjectsWithSameValue(), 2);
		assertSizeAtLeast(getObjectsWithDifferentValue(), 1);
	}

	private void assertSizeAtLeast(List<T> objects, int i) {
		assertThat(objects, is(notNullValue()));
		assertThat(objects.size(), is(greaterThan(i-1)));
	}
	
	
	@Test
	public void notEqualToNull() throws Exception {
		for(T o1: getObjectsWithSameValue()) {
			assertThat(o1.equals(null), is(false));
		}
		for(T o1: getObjectsWithDifferentValue()) {
			assertThat(o1.equals(null), is(false));
		}
	}

	@Test
	public void reflexiveAndSymmetric() throws Exception {
		for(T o1: getObjectsWithSameValue()) {
			for(T o2: getObjectsWithSameValue()) {
				assertThat(o1.equals(o2), is(true));
				assertThat(o2.equals(o1), is(true));
				assertThat(o1.hashCode(), is(equalTo(o2.hashCode())));
			}
		}
	}

	@Test
	public void notEqual() throws Exception {
		for(T o1: getObjectsWithSameValue()) {
			for(T o2: getObjectsWithDifferentValue()) {
				assertThat(o1.equals(o2), is(false));
				assertThat(o2.equals(o1), is(false));
			}
		}
	}

	@Test
	public void transitiveWhenEqual() throws Exception {
		for(T o1: getObjectsWithSameValue()) {
			for(T o2: getObjectsWithSameValue()) {
				for(Object o3: getObjectsWithSameValue()) {
					assertThat(o1.equals(o2), is(true));
					assertThat(o2.equals(o3), is(true));
					assertThat(o1.equals(o3), is(true));
				}
			}
		}
	}

	protected abstract List<T> getObjectsWithSameValue();
	protected abstract List<T> getObjectsWithDifferentValue();


}
