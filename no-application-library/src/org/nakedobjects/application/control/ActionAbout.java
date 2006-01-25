package org.nakedobjects.application.control;



public interface ActionAbout {
    Object[][] getOptions();
    
    Object[] getDefaultParameterValues();

    String[] getParameterLabels();
    
    boolean[] getRequired();

    void invisible();

    void invisibleToUser(User user);

    void invisibleToUsers(User[] users);

    void setDescription(String string);

    void setName(String string);

    void setParameter(int index, Object defaultValue);

    void setParameter(int index, Object[] options);

    void setParameter(int index, String label);

    void setParameter(int index, boolean required);

    void setParameter(int index, String label, Object defaultValue, boolean required);

    void setParameters(Object[] defaultValues);

    void setParameters(String[] labels);

    void setParameters(boolean[] required);

    void unusable();

    void unusable(String reason);

    void unusableInState(State state);

    void unusableInStates(State[] states);

    void unusableOnCondition(boolean conditionMet, String reasonNotMet);

    void usableOnlyInState(State state);

    void usableOnlyInStates(State[] states);

    void visibleOnlyToRole(Role role);

    void visibleOnlyToRoles(Role[] roles);

    void visibleOnlyToUser(User user);

    void visibleOnlyToUsers(User[] users);

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