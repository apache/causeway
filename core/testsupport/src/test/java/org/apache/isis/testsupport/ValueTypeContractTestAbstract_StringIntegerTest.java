package org.apache.isis.testsupport;

import java.math.BigInteger;

public class ValueTypeContractTestAbstract_StringIntegerTest extends ValueTypeContractTestAbstract {

	@Override
	protected Object getObject() {
		return new String("1");
	}

	@Override
	protected Object getObjectWithSameValue() {
		return new String("1");
	}

	@Override
	protected Object getAnotherObjectWithSameValue() {
		return new String("1");
	}

	@Override
	protected Object getObjectWithDifferentValue() {
		return new String("2");
	}

}
