package org.nakedobjects.utility.xmlsnapshot;


import org.nakedobjects.object.io.Transferable;
import org.nakedobjects.object.io.TransferableWriter;

import org.w3c.dom.Element;

final class DomTransferableWriter implements TransferableWriter {

	private final Element parentElement;
	private final NofMetaModel nofMetaModel;
	private final Helper helper;
	private final Element oidElement;

	DomTransferableWriter(final Element parentElement, final NofMetaModel nofMetaModel, final String fullyQualifiedClassName) {
		this.helper = new Helper();
		this.nofMetaModel = nofMetaModel;
		this.parentElement = parentElement;
		this.oidElement = nofMetaModel.appendElement(parentElement, "oid");
		if (fullyQualifiedClassName != null) {
			oidElement.setAttributeNS(NofMetaModel.NOF_METAMODEL_NS_URI, "nof:fqn", fullyQualifiedClassName);
		}
	}
	private DomTransferableWriter(final Element parentElement, final NofMetaModel nofMetaModel) {
		this(parentElement, nofMetaModel, null);
	}

	public void writeInt(int i) {
		appendElement("int", ""+i);
	}

	public void writeString(String str) {
		appendElement("string", str);
	}

	public void writeLong(long l) {
		appendElement("long", ""+l);
	}

	private void appendElement(final String elementName, final String value) {
		Element oidComponentElement = nofMetaModel.appendElement(oidElement, elementName);
		oidComponentElement.appendChild(helper.docFor(oidComponentElement).createTextNode(value));
	}
    
	/**
	 * Recursively appends Transferable's own DOM element graph to
	 * this writer's <code>oidElement</code>
	 */
	public void writeObject(Transferable t) {
		DomTransferableWriter dtw = new DomTransferableWriter(oidElement, nofMetaModel);
		t.writeData(dtw);
		dtw.close();
	}

	/**
	 * Does nothing (the child OID element has already been appended to parent).
	 */
	public void close() {
		// NOTHING TO DO.
	}
}


