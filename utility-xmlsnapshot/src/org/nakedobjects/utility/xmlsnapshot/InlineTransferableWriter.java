package org.nakedobjects.utility.xmlsnapshot;

import org.nakedobjects.object.io.Transferable;
import org.nakedobjects.object.io.TransferableWriter;


/**
	* <pre>
	*   [[95I]]
	*   [[abc]]
	*   [[95J][abc]]
	*   [[95J][[abc][89I]][23J]]
	* </pre>
	*/
final class InlineTransferableWriter implements TransferableWriter {

	public final static String SEQUENCE_START = "{";
	public final static String SEQUENCE_END = "}";

	private final StringBuffer buffer;
	private String asString;

	InlineTransferableWriter() {
		buffer = new StringBuffer();
		buffer.append(SEQUENCE_START);
	}

	public void writeInt(int i) {
		buffer.append(SEQUENCE_START).append(i).append("I").append(SEQUENCE_END);
	}

	public void writeString(String str) {
		buffer.append(SEQUENCE_START).append(str).append(SEQUENCE_END);
	}

	public void writeLong(long l) {
		buffer.append(SEQUENCE_START).append(l).append("J").append(SEQUENCE_END);
	}

	/**
	 * Recursively invoke another instance of InlineTransferableWriter.
	 */
	public void writeObject(Transferable t) {
		InlineTransferableWriter itw = new InlineTransferableWriter();
		t.writeData(itw);
		itw.close();
		buffer.append(itw.toString());
	}

	/**
	 * Appends child OID element to parent
	 */
	public void close() {
		buffer.append(SEQUENCE_END);
		this.asString = buffer.toString();
	}

	/**
	 * returns null until closed.
	 */
	public String toString() {
		return asString;
	}
}
