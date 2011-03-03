package org.apache.isis.core.runtime.testsystem.tests;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.runtime.testsystem.TestProxyOid;
import org.apache.isis.core.testsupport.value.ValueTypeContractTestAbstract;

public class TestProxyOid_ValueType extends ValueTypeContractTestAbstract<TestProxyOid> {

	@Override
	protected List<TestProxyOid> getObjectsWithSameValue() {
		return Arrays.asList(new TestProxyOid(1, true), new TestProxyOid(1, true));
	}

	@Override
	protected List<TestProxyOid> getObjectsWithDifferentValue() {
		return Arrays.asList(new TestProxyOid(1, false), new TestProxyOid(2, true));
	}


}
