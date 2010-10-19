package org.apache.isis.testsupport;

import java.util.Arrays;
import java.util.List;

public class ValueTypeContractTestAbstract_StringIntegerTest extends ValueTypeContractTestAbstract<String> {

	@Override
	protected List<String> getObjectsWithSameValue() {
		return Arrays.asList(new String("1"), new String("1"));
	}

	@Override
	protected List<String> getObjectsWithDifferentValue() {
		return Arrays.asList(new String("1 "), new String(" 1"), new String("2"));
	}

}
