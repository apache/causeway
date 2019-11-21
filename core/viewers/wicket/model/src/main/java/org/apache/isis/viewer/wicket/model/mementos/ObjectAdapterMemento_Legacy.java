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

package org.apache.isis.viewer.wicket.model.mementos;

final class ObjectAdapterMemento_Legacy /*implements ObjectAdapterMemento*/ {

    //	private static final long serialVersionUID = 1L;
    //
    //	/**
    //	 * Factory method
    //	 */
    //	public static ObjectAdapterMemento_Legacy ofAdapter(@Nullable final ObjectAdapter adapter) {
    //		if (adapter == null) {
    //			return null;
    //		}
    //		final Object object = adapter.getPojo();
    //		if(object == null) {
    //			return null;
    //		}
    //		return new ObjectAdapterMemento_Legacy(adapter);
    //	}
    //
    //	/**
    //	 * Factory method
    //	 */
    //	public static ObjectAdapterMemento_Legacy ofRootOid(final RootOid rootOid) {
    //		return new ObjectAdapterMemento_Legacy(rootOid);
    //	}
    //
    //	/**
    //	 * Factory method
    //	 */
    //	public static ObjectAdapterMemento_Legacy ofMementoList(
    //			final Collection<ObjectAdapterMemento> list,
    //			final ObjectSpecId objectSpecId) {
    //		
    //		return list != null 
    //				? new ObjectAdapterMemento_Legacy(_Lists.newArrayList(list), objectSpecId) 
    //						: null;
    //	}
    //
    //	enum Sort {
    //		/**
    //		 * represents a single object
    //		 */
    //		SCALAR {
    //
    //			@Override
    //			public ObjectAdapter asAdapter(
    //					final ObjectAdapterMemento oam,
    //					final ConcurrencyChecking concurrencyChecking,
    //					final SpecificationLoader specificationLoader) {
    //				
    //				return ((ObjectAdapterMemento_Legacy)oam)
    //						.type.recreateAdapter(oam, concurrencyChecking, specificationLoader);
    //			}
    //
    //			@Override
    //			public int hashCode(final ObjectAdapterMemento_Legacy oam) {
    //				return oam.type.hashCode(oam);
    //			}
    //
    //			@Override
    //			public boolean equals(final ObjectAdapterMemento_Legacy oam, final Object other) {
    //				if (!(other instanceof ObjectAdapterMemento_Legacy)) {
    //					return false;
    //				}
    //				final ObjectAdapterMemento_Legacy otherOam = (ObjectAdapterMemento_Legacy) other;
    //				if(otherOam.sort != SCALAR) {
    //					return false;
    //				}
    //				return oam.type.equals(oam, (ObjectAdapterMemento_Legacy) other);
    //			}
    //
    //			@Override
    //			public String asString(final ObjectAdapterMemento_Legacy oam) {
    //				return oam.type.toString(oam);
    //			}
    //		},
    //		/**
    //		 * represents a list of objects
    //		 */
    //		VECTOR {
    //
    //			@Override
    //			public ObjectAdapter asAdapter(
    //					final ObjectAdapterMemento oam,
    //					final ConcurrencyChecking concurrencyChecking, 
    //					final SpecificationLoader specificationLoader) {
    //
    //			    val oap = IsisContext.getObjectAdapterProvider();
    //			    
    //				final List<Object> listOfPojos = _NullSafe.stream(oam.getList())
    //				        .filter(_NullSafe::isPresent)
    //				        .map(ObjectAdapterMemento::getObjectAdapter)
    //				        .filter(_NullSafe::isPresent)
    //				        .map(ObjectAdapter::getPojo)
    //				        .filter(_NullSafe::isPresent)
    //				        .collect(Collectors.toList());
    //				
    //				return oap.adapterFor(listOfPojos);
    //			}
    //
    //			@Override
    //			public int hashCode(final ObjectAdapterMemento_Legacy oam) {
    //				return oam.list.hashCode();
    //			}
    //
    //			@Override
    //			public boolean equals(final ObjectAdapterMemento_Legacy oam, final Object other) {
    //				if (!(other instanceof ObjectAdapterMemento_Legacy)) {
    //					return false;
    //				}
    //				final ObjectAdapterMemento_Legacy otherOam = (ObjectAdapterMemento_Legacy) other;
    //				if(otherOam.sort != VECTOR) {
    //					return false;
    //				}
    //				return oam.list.equals(otherOam.list);
    //			}
    //
    //			@Override
    //			public String asString(final ObjectAdapterMemento_Legacy oam) {
    //				return oam.list.toString();
    //			}
    //		};
    //
    //		void ensure(final Sort sort) {
    //			if(this == sort) {
    //				return;
    //			}
    //			throw new IllegalStateException("Memento is not for " + sort);
    //		}
    //
    //		public abstract ObjectAdapter asAdapter(
    //				final ObjectAdapterMemento oam,
    //				final ConcurrencyChecking concurrencyChecking, 
    //				final SpecificationLoader specificationLoader);
    //
    //		public abstract int hashCode(final ObjectAdapterMemento_Legacy oam);
    //
    //		public abstract boolean equals(final ObjectAdapterMemento_Legacy oam, final Object other);
    //
    //		public abstract String asString(final ObjectAdapterMemento_Legacy oam);
    //	}
    //
    //	enum Type {
    //		/**
    //		 * The {@link ObjectAdapter} that this is the memento for directly has
    //		 * an {@link EncodableFacet} (it is almost certainly a value), and so is
    //		 * stored directly.
    //		 */
    //		ENCODEABLE {
    //			@Override
    //			ObjectAdapter recreateAdapter(
    //					final ObjectAdapterMemento oam,
    //					final ConcurrencyChecking concurrencyChecking,
    //					final SpecificationLoader specificationLoader) {
    //				
    //				ObjectSpecId objectSpecId = oam.getObjectSpecId();
    //				ObjectSpecification objectSpec = SpecUtils.getSpecificationFor(objectSpecId, specificationLoader);
    //				final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
    //				return encodableFacet.fromEncodedString(((ObjectAdapterMemento_Legacy)oam).encodableValue);
    //			}
    //
    //			@Override
    //			public boolean equals(ObjectAdapterMemento_Legacy oam, ObjectAdapterMemento_Legacy other) {
    //				return other.type == ENCODEABLE && oam.encodableValue.equals(other.encodableValue);
    //			}
    //
    //			@Override
    //			public int hashCode(ObjectAdapterMemento_Legacy oam) {
    //				return oam.encodableValue.hashCode();
    //			}
    //
    //			@Override
    //			public String toString(final ObjectAdapterMemento_Legacy oam) {
    //				return oam.encodableValue;
    //			}
    //
    //			@Override
    //			public void resetVersion(
    //					ObjectAdapterMemento_Legacy objectAdapterMemento,
    //					final SpecificationLoader specificationLoader) {
    //			}
    //		},
    //		/**
    //		 * The {@link ObjectAdapter} that this is for is already known by its
    //		 * (persistent) {@link Oid}.
    //		 */
    //		PERSISTENT {
    //			@Override
    //			ObjectAdapter recreateAdapter(
    //					final ObjectAdapterMemento oam,
    //					ConcurrencyChecking concurrencyChecking,
    //					final SpecificationLoader specificationLoader) {
    //				
    //				val persistentOidStr = ((ObjectAdapterMemento_Legacy)oam).persistentOidStr;
    //
    //				val rootOid = Oid.unmarshaller().unmarshal(persistentOidStr, RootOid.class);
    //				try {
    //					val persistenceSession = IsisContext.getPersistenceSession().orElse(null);
    //					final ObjectAdapter adapter = persistenceSession.adapterFor(rootOid, concurrencyChecking);
    //					return adapter;
    //
    //				} finally {
    //					// a side-effect of AdapterManager#adapterFor(...) is that it will update the oid
    //					// with the correct version, even when there is a concurrency exception
    //					// we copy this updated oid string into our memento so that, if we retry,
    //					// we will succeed second time around
    //
    //					((ObjectAdapterMemento_Legacy)oam).persistentOidStr = rootOid.enString();
    //				}
    //			}
    //
    //			@Override
    //			public void resetVersion(
    //					final ObjectAdapterMemento_Legacy oam,
    //					final SpecificationLoader specificationLoader) {
    //				// REVIEW: this may be redundant because recreateAdapter also guarantees the version will be reset.
    //				final ObjectAdapter adapter = recreateAdapter(
    //						oam, ConcurrencyChecking.NO_CHECK, specificationLoader);
    //				Oid oid = adapter.getOid();
    //				oam.persistentOidStr = oid.enString();
    //			}
    //
    //			@Override
    //			public boolean equals(ObjectAdapterMemento_Legacy oam, ObjectAdapterMemento_Legacy other) {
    //				return other.type == PERSISTENT && oam.persistentOidStr.equals(other.persistentOidStr);
    //			}
    //
    //			@Override
    //			public int hashCode(ObjectAdapterMemento_Legacy oam) {
    //				return oam.persistentOidStr.hashCode();
    //			}
    //
    //			@Override
    //			public String toString(final ObjectAdapterMemento_Legacy oam) {
    //				return oam.persistentOidStr;
    //			}
    //
    //		},
    //		/**
    //		 * Uses Isis' own {@link Memento}, to capture the state of a transient
    //		 * object.
    //		 */
    //		TRANSIENT {
    //			/**
    //			 * {@link ConcurrencyChecking} is ignored for transients.
    //			 */
    //			@Override
    //			ObjectAdapter recreateAdapter(
    //					final ObjectAdapterMemento oam,
    //					final ConcurrencyChecking concurrencyChecking,
    //					final SpecificationLoader specificationLoader) {
    //				
    //				return ((ObjectAdapterMemento_Legacy)oam).transientMemento.recreateObject();
    //			}
    //
    //			@Override
    //			public boolean equals(ObjectAdapterMemento_Legacy oam, ObjectAdapterMemento_Legacy other) {
    //				return other.type == TRANSIENT && oam.transientMemento.equals(other.transientMemento);
    //			}
    //
    //			@Override
    //			public int hashCode(ObjectAdapterMemento_Legacy oam) {
    //				return oam.transientMemento.hashCode();
    //			}
    //
    //			@Override
    //			public String toString(final ObjectAdapterMemento_Legacy oam) {
    //				return oam.transientMemento.toString();
    //			}
    //
    //			@Override
    //			public void resetVersion(
    //					final ObjectAdapterMemento_Legacy objectAdapterMemento,
    //					final SpecificationLoader specificationLoader) {
    //			}
    //		};
    //
    //		abstract ObjectAdapter recreateAdapter(
    //				final ObjectAdapterMemento nom,
    //				final ConcurrencyChecking concurrencyChecking,
    //				final SpecificationLoader specificationLoader);
    //
    //		public abstract boolean equals(ObjectAdapterMemento_Legacy oam, ObjectAdapterMemento_Legacy other);
    //		public abstract int hashCode(ObjectAdapterMemento_Legacy objectAdapterMemento);
    //
    //		public abstract String toString(ObjectAdapterMemento_Legacy adapterMemento);
    //
    //		public abstract void resetVersion(
    //				ObjectAdapterMemento_Legacy objectAdapterMemento,
    //				final SpecificationLoader specificationLoader);
    //	}
    //
    //
    //
    //	private final Sort sort;
    //	private final ObjectSpecId objectSpecId;
    //
    //	/**
    //	 * Populated only if {@link #getSort() sort} is {@link Sort#SCALAR scalar}
    //	 */
    //	private Type type;
    //
    //	/**
    //	 * Populated only if {@link #getSort() sort} is {@link Sort#SCALAR scalar}
    //	 */
    //	@SuppressWarnings("unused")
    //	private String titleHint;
    //
    //	/**
    //	 * The current value, if {@link Type#ENCODEABLE}; will be <tt>null</tt> otherwise.
    //	 *
    //	 * <p>
    //	 * Also, populated only if {@link #getSort() sort} is {@link Sort#SCALAR scalar}
    //	 */
    //	private String encodableValue;
    //
    //	/**
    //	 * The current value, if {@link Type#PERSISTENT}, will be <tt>null</tt> otherwise.
    //	 *
    //	 * <p>
    //	 * Also, populated only if {@link #getSort() sort} is {@link Sort#SCALAR scalar}
    //	 */
    //	private String persistentOidStr;
    //
    //	/**
    //	 * The current value, if {@link Type#PERSISTENT}, will be <tt>null</tt> otherwise.
    //	 *
    //	 * <p>
    //	 * Also, populated only if {@link #getSort() sort} is {@link Sort#SCALAR scalar}
    //	 */
    //	private Bookmark bookmark;
    //
    //	/**
    //	 * Only populated for {@link ObjectAdapter#getPojo() domain object}s that implement {@link HintStore.HintIdProvider}.
    //	 */
    //	private String hintId;
    //
    //	/**
    //	 * The current value, if {@link Type#TRANSIENT}, will be <tt>null</tt> otherwise.
    //	 *
    //	 * <p>
    //	 * Also, populated only if {@link #getSort() sort} is {@link Sort#SCALAR scalar}
    //	 */
    //	private Memento transientMemento;
    //
    //	/**
    //	 * populated only if {@link #getSort() sort} is {@link Sort#VECTOR vector}
    //	 */
    //	private ArrayList<ObjectAdapterMemento> list;
    //
    //	public ObjectAdapterMemento_Legacy(
    //			final ArrayList<ObjectAdapterMemento> list, 
    //			final ObjectSpecId objectSpecId) {
    //		
    //		this.sort = Sort.VECTOR;
    //		this.list = list;
    //		this.objectSpecId = objectSpecId;
    //	}
    //
    //	private ObjectAdapterMemento_Legacy(final RootOid rootOid) {
    //
    //	    if (rootOid.isTransient()) {
    //            throw _Exceptions.unexpectedCodeReach();
    //        }
    //
    //	    this.objectSpecId = rootOid.getObjectSpecId();
    //	    
    //	    //TODO[2112] do we ever need to create ENCODEABLE here?
    //        val spec = IsisContext.getSpecificationLoader().lookupBySpecId(objectSpecId);
    //        if(spec!=null && spec.isEncodeable()) {
    //            this.sort = Sort.SCALAR;
    //            this.type = Type.ENCODEABLE;
    //            this.encodableValue = rootOid.getIdentifier();
    //            return;
    //        } 
    //		
    //		this.sort = Sort.SCALAR;
    //		this.type = Type.PERSISTENT;
    //
    //		this.persistentOidStr = rootOid.enString();
    //		this.bookmark = rootOid.asBookmark();
    //	}
    //
    //	private ObjectAdapterMemento_Legacy(final ObjectAdapter adapter) {
    //		
    //		requires(adapter, "adapter");
    //		
    //		this.sort = Sort.SCALAR;
    //		final ObjectSpecification specification = adapter.getSpecification();
    //		objectSpecId = specification.getSpecId();
    //		
    //		final EncodableFacet encodableFacet = specification.getFacet(EncodableFacet.class);
    //		final boolean isEncodable = encodableFacet != null;
    //		if (isEncodable) {
    //			encodableValue = encodableFacet.toEncodedString(adapter);
    //			type = Type.ENCODEABLE;
    //			return;
    //		}
    //
    //		final RootOid rootOid = (RootOid) adapter.getOid();
    //		if (rootOid.isTransient()) {
    //			transientMemento = new Memento(adapter);
    //			type = Type.TRANSIENT;
    //			return;
    //		}
    //
    //		persistentOidStr = rootOid.enString();
    //		bookmark = rootOid.asBookmark();
    //		if(adapter.getPojo() instanceof HintStore.HintIdProvider) {
    //			HintStore.HintIdProvider provider = (HintStore.HintIdProvider) adapter.getPojo();
    //			this.hintId = provider.hintId();
    //		}
    //		type = Type.PERSISTENT;
    //	}
    //
    //	public Sort getSort() {
    //		return sort;
    //	}
    //
    //	public ArrayList<ObjectAdapterMemento> getList() {
    //		ensureVector();
    //		return list;
    //	}
    //
    //
    //	private void resetVersion_legacy(
    //			final SpecificationLoader specificationLoader) {
    //		
    //		ensureScalar();
    //		type.resetVersion(this, specificationLoader);
    //	}
    //
    //
    //	public Bookmark asBookmarkIfSupported() {
    //		ensureScalar();
    //		return bookmark;
    //	}
    //
    //	public Bookmark asHintingBookmarkIfSupported() {
    //		Bookmark bookmark = asBookmarkIfSupported();
    //		return hintId != null && bookmark != null
    //				? new HintStore.BookmarkWithHintId(bookmark, hintId)
    //						: bookmark;
    //	}
    //
    ////	/**
    ////	 * Lazily looks up {@link ObjectAdapter} if required.
    ////	 *
    ////	 * <p>
    ////	 * For transient objects, be aware that calling this method more than once
    ////	 * will cause the underlying {@link ObjectAdapter} to be recreated,
    ////	 * overwriting any changes that may have been made. In general then it's
    ////	 * best to call once and then hold onto the value thereafter. Alternatively,
    ////	 * can call {@link #setAdapter(ObjectAdapter)} to keep this memento in sync.
    ////	 */
    ////	private ObjectAdapter getObjectAdapter_legacy(
    ////			final ConcurrencyChecking concurrencyChecking,
    ////			final PersistenceSession persistenceSession,
    ////			final SpecificationLoader specificationLoader) {
    ////		return sort.asAdapter(this, concurrencyChecking, persistenceSession, specificationLoader);
    ////	}
    //
    ////	/**
    ////	 * Updates the memento if the adapter's state has changed.
    ////	 *
    ////	 * @param adapter
    ////	 */
    ////	public void setAdapter(final ObjectAdapter adapter) {
    ////		ensureScalar();
    ////		init(adapter);
    ////	}
    //
    //	public ObjectSpecId getObjectSpecId() {
    //		return objectSpecId;
    //	}
    //
    ////	/**
    ////	 * Analogous to {@link List#contains(Object)}, but does not perform
    ////	 * {@link ConcurrencyChecking concurrency checking} of the OID.
    ////	 */
    ////	public boolean containedIn(
    ////			List<ObjectAdapterMemento_Legacy> list,
    ////			final PersistenceSession persistenceSession,
    ////			final SpecificationLoader specificationLoader) {
    ////
    ////		ensureScalar();
    ////
    ////		// REVIEW: heavy handed, ought to be possible to just compare the OIDs
    ////		// ignoring the concurrency checking
    ////		final ObjectAdapter currAdapter = getObjectAdapter(ConcurrencyChecking.NO_CHECK, persistenceSession,
    ////				specificationLoader);
    ////		for (ObjectAdapterMemento_Legacy each : list) {
    ////			if(each == null) {
    ////				continue;
    ////			}
    ////			final ObjectAdapter otherAdapter = each.getObjectAdapter(
    ////					ConcurrencyChecking.NO_CHECK, persistenceSession, specificationLoader);
    ////			if(currAdapter == otherAdapter) {
    ////				return true;
    ////			}
    ////		}
    ////		return false;
    ////	}
    //
    //	@Override
    //	public int hashCode() {
    //		return sort.hashCode(this);
    //	}
    //
    //	@Override
    //	public boolean equals(Object obj) {
    //		return sort.equals(this, obj);
    //	}
    //
    //
    //	@Override
    //	public String toString() {
    //		return asString();
    //	}
    //
    //	public String asString() {
    //		return sort.asString(this);
    //	}
    //
    //	private void ensureScalar() {
    //		getSort().ensure(Sort.SCALAR);
    //	}
    //
    //	private void ensureVector() {
    //		getSort().ensure(Sort.VECTOR);
    //	}
    //
    //	@Override
    //	public ObjectAdapter getObjectAdapter() {
    //
    //	    val specLoader = IsisContext.getSpecificationLoader();
    //	    
    //	    if(type==null) {
    //	        return sort.asAdapter(this, ConcurrencyChecking.NO_CHECK, specLoader);
    //	    }
    //	    
    //		// we use de-serialization for the remaining cases
    //		return type.recreateAdapter(this, ConcurrencyChecking.NO_CHECK, specLoader);
    //	}
    //
    //	@Override
    //	public void resetVersion() {
    //		resetVersion_legacy(IsisContext.getSpecificationLoader());
    //	}

}
