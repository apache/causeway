package org.apache.isis.testsupport;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.*;

import org.junit.Test;

/**
 * Contract test for value types ({@link #equals(Object)} and {@link #hashCode()}).
 */
public abstract class ValueTypeContractTestAbstract {
	
	@Test
	public void symmetric() throws Exception {
		Object o1 = getObject();
		assertThat(o1.equals(o1), is(true));
	}

	@Test
	public void notEqualToNull() throws Exception {
		Object o1 = getObject();
		assertThat(o1.equals(null), is(false));
	}

	@Test
	public void reflexiveWhenEqual() throws Exception {
		Object o1 = getObject();
		Object o2 = getObjectWithSameValue();
		assertThat(o1.equals(o2), is(true));
		assertThat(o2.equals(o1), is(true));
	}

	@Test
	public void reflexiveWhenNotEqual() throws Exception {
		Object o1 = getObject();
		Object o2 = getObjectWithDifferentValue();
		assertThat(o1.equals(o2), is(false));
		assertThat(o2.equals(o1), is(false));
	}

	@Test
	public void transitiveWhenEqual() throws Exception {
		Object o1 = getObject();
		Object o2 = getObjectWithSameValue();
		Object o3 = getAnotherObjectWithSameValue();
		assertThat(o1.equals(o2), is(true));
		assertThat(o2.equals(o3), is(true));
		
		assertThat(o1.equals(o3), is(true));
	}

	protected abstract Object getObject();
	protected abstract Object getObjectWithSameValue();
	protected abstract Object getAnotherObjectWithSameValue();

	protected abstract Object getObjectWithDifferentValue();

}
