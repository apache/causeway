/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    The authors can be contacted via www.nakedobjects.org (the
    registered address of Naked Objects Group is Kingsway House, 123 Goldworth
    Road, Woking GU21 1NR, UK).
*/

package org.nakedobjects.newgui.view;

import java.awt.Image;
import java.util.Enumeration;
import java.util.Observer;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Summary;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Permission;


public class MockInstanceCollection implements NakedCollection {

	private NakedObject object;

	public int getDisplaySize() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int position() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void add(NakedObject object) {
		this.object = object;
	}

	public void added(NakedObject object) {
		// TODO Auto-generated method stub
		
	}

	public Permission canAdd(NakedObject object) {
		// TODO Auto-generated method stub
		return null;
	}

	public Permission canRemove(NakedObject object) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean contains(NakedObject object) {
		return this.object == object;
	}

	public void copyObject(Naked objectToCopy) {
		// TODO Auto-generated method stub
		
	}

	public Enumeration displayElements() {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration elements() {
		// TODO Auto-generated method stub
		return null;
	}

	public void first() {
		// TODO Auto-generated method stub
		
	}

	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasPrevious() {
		// TODO Auto-generated method stub
		return false;
	}

	public void last() {
		// TODO Auto-generated method stub
		
	}

	public void next() {
		// TODO Auto-generated method stub
		
	}

	public void previous() {
		// TODO Auto-generated method stub
		
	}

	public void remove(NakedObject object) {
		// TODO Auto-generated method stub
		
	}

	public void removed(NakedObject element) {
		// TODO Auto-generated method stub
		
	}

	public void reset() {
		// TODO Auto-generated method stub
		
	}

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Title title() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addObserver(Observer o) {
		// TODO Auto-generated method stub
		
	}

	public String contextualTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	public int countObservers() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void created() {
		// TODO Auto-generated method stub
		
	}

	public void deleted() {
		// TODO Auto-generated method stub
		
	}

	public void deleteObserver(Observer o) {
		// TODO Auto-generated method stub
		
	}

	public void destroy() throws ObjectStoreException {
		// TODO Auto-generated method stub
		
	}

	public String getCollectiveName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getIdString() {
		// TODO Auto-generated method stub
		return null;
	}

	public NakedClass getNakedClass() {
		// TODO Auto-generated method stub
		return null;
	}

	public NakedObjectManager getNakedObjectManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getOid() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChanged() {
		// TODO Auto-generated method stub
		return false;
	}

	public Image iconImage(int size) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isFinder() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isLookupElement(String arg) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPersistent() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isResolved() {
		// TODO Auto-generated method stub
		return false;
	}

	public void objectChanged() {
		// TODO Auto-generated method stub
		
	}

	public void makeFinder() {
		// TODO Auto-generated method stub
		
	}

	public void makePersistent() {
		// TODO Auto-generated method stub
		
	}

	public void resolve() {
		// TODO Auto-generated method stub
		
	}

	public void setChangedAndNotifyObservers() {
		// TODO Auto-generated method stub
		
	}

	public void setOid(Object oid) {
		// TODO Auto-generated method stub
		
	}

	public void setResolved() {
		// TODO Auto-generated method stub
		
	}

	public About about() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getShortClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getObjectHelpText() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSameAs(Naked object) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	public Summary summary() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getIconName() {
		// TODO Auto-generated method stub
		return null;
	}

}
