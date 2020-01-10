package org.apache.isis.testing.unittestsupport.applib.bean;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

public class FixtureDatumFactoriesForApplib {

	public static PojoTester.FixtureDatumFactory<Blob> blobs() {
		return new PojoTester.FixtureDatumFactory<>(Blob.class,
				new Blob("foo", "application/pdf", new byte[]{1,2,3}),
				new Blob("bar", "application/docx", new byte[]{4,5}),
				new Blob("baz", "application/xlsx", new byte[]{7,8,9,0})
				);
	}

	public static PojoTester.FixtureDatumFactory<Clob> clobs() {
		return new PojoTester.FixtureDatumFactory<>(Clob.class,
				new Clob("foo", "text/html", "<html/>".toCharArray()),
				new Clob("bar", "text/plain", "hello world".toCharArray()),
				new Clob("baz", "text/ini", "foo=bar".toCharArray())
				);
	}

}
