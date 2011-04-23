package org.apache.isis.runtimes.dflt.runtime.snapshot;

import java.util.List;

import org.apache.isis.applib.snapshot.Snapshottable;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.runtime.snapshot.XmlSchema;
import org.apache.isis.core.runtime.snapshot.XmlSnapshot;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSession;

import com.google.common.collect.Lists;

/**
 * Builds an {@link XmlSnapshot} using a fluent use through
 * a builder:
 * 
 * <pre>
 * XmlSnapshot snapshot = 
 *     XmlSnapshotBuilder.create(customer)
 *                .includePath(&quot;placeOfBirth&quot;)
 *                .includePath(&quot;orders/product&quot;)
 *                .build();
 * Element customerAsXml = snapshot.toXml();
 * </pre>
 */
public class XmlSnapshotBuilder {
	
	private final Snapshottable snapshottable;
    private XmlSchema schema;
    static class PathAndAnnotation {
    	public PathAndAnnotation(String path, String annotation) {
			this.path = path;
			this.annotation = annotation;
		}
		private String path;
    	private String annotation;
    }
    private List<XmlSnapshotBuilder.PathAndAnnotation> paths = Lists.newArrayList();
	
	public XmlSnapshotBuilder(Snapshottable domainObject) {
		this.snapshottable = domainObject;
	}
	public XmlSnapshotBuilder usingSchema(XmlSchema schema) {
		this.schema = schema;
		return this;
	}
	public XmlSnapshotBuilder includePath(String path) {
		return includePathAndAnnotation(path, null);
	}
	public XmlSnapshotBuilder includePathAndAnnotation(String path, String annotation) {
		paths.add(new PathAndAnnotation(path, annotation));
		return this;
	}
	
	public XmlSnapshot build() {
		ObjectAdapter adapter = getAdapterMap().adapterFor(snapshottable);
		XmlSnapshot snapshot = (schema != null) ? 
				new XmlSnapshot(adapter, schema) : 
				new XmlSnapshot(adapter);
		for(XmlSnapshotBuilder.PathAndAnnotation paa: paths) {
			if (paa.annotation != null) {
				snapshot.include(paa.path, paa.annotation);
			} else {
				snapshot.include(paa.path);
			}
		}
		return snapshot;
	}

	/////////////////////////////////////////////////////////
	// Dependencies (from context)
	/////////////////////////////////////////////////////////
	
	private static AdapterMap getAdapterMap() {
		return getPersistenceSession().getAdapterManager();
	}
	
	private static PersistenceSession getPersistenceSession() {
		return IsisContext.getPersistenceSession();
	}
}