package org.nakedobjects.application.control;



public interface FieldAbout {
    void invisible();

    void invisibleToUser(User user);

    void invisibleToUsers(User[] users);

    boolean isPersistent();

    void modifiableOnlyByRole(Role role);

    void modifiableOnlyByRoles(Role[] roles);

    void modifiableOnlyByUser(User user);

    void modifiableOnlyByUsers(User[] users);

    void modifiableOnlyInState(State state);

    void modifiableOnlyInStates(State[] states);

    void nonPersistent();

    void setDescription(String string);

    void unmodifiable();

    void unmodifiable(String reason);

    void unmodifiableByUser(User user);

    void unmodifiableByUsers(User[] users);

    void unmodifiableInState(State state);

    void unmodifiableInStates(State[] states);

    void unmodifiableOnCondition(boolean conditionMet, String reasonNotMet);

    void visibleOnlyToRole(Role role);

    void visibleOnlyToRoles(Role[] roles);

    void visibleOnlyToUser(User user);

    void visibleOnlyToUsers(User[] users);
    
    void invalid();
    
    void invalid(String reason);
    
    void invalidOnCondition(boolean condition, String reason);
    
    
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */