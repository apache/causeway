package org.apache.isis.objectstore.jdo.datanucleus.valuetypes;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ClassNameConstants;
import org.datanucleus.NucleusContext;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.mapped.DatastoreContainerObject;
import org.datanucleus.store.mapped.MappedStoreManager;
import org.datanucleus.store.mapped.mapping.SingleFieldMultiMapping;

public class IsisClobMapping extends SingleFieldMultiMapping {

    public IsisClobMapping() {
    }
    
    @Override
    public Class<?> getJavaType() {
        return org.apache.isis.applib.value.Clob.class;
    }

    public void initialize(AbstractMemberMetaData mmd, DatastoreContainerObject container, ClassLoaderResolver clr)
    {
        super.initialize(mmd, container, clr);
        addDatastoreFields();
    }

    public void initialize(MappedStoreManager storeMgr, String type)
    {
        super.initialize(storeMgr, type);
        addDatastoreFields();
    }

    protected void addDatastoreFields()
    {
        addDatastoreField(ClassNameConstants.JAVA_LANG_STRING); // name
        addDatastoreField(ClassNameConstants.JAVA_LANG_STRING); // mime type
        addDatastoreField(ClassNameConstants.JAVA_LANG_CHARACTER_ARRAY); // chars
    }

    public Object getValueForDatastoreMapping(NucleusContext nucleusCtx, int index, Object value)
    {
        Clob clob = ((Clob)value);
        switch (index) {
            case 0: return clob.getName();
            case 1: return clob.getMimeType().getBaseType();
            case 2: return clob.getChars();
        }
        throw new IndexOutOfBoundsException();
    }

    public void setObject(ExecutionContext ec, Object preparedStmt, int[] exprIndex, Object value)
    {
        Clob clob = ((Clob)value);
        if (clob == null) {
            getDatastoreMapping(0).setObject(preparedStmt, exprIndex[0], null);
            getDatastoreMapping(1).setObject(preparedStmt, exprIndex[1], null);
            getDatastoreMapping(2).setObject(preparedStmt, exprIndex[2], null);
        } else {
            getDatastoreMapping(0).setString(preparedStmt, exprIndex[0], clob.getName());
            getDatastoreMapping(1).setString(preparedStmt, exprIndex[1], clob.getMimeType().getBaseType());
            getDatastoreMapping(2).setObject(preparedStmt, exprIndex[2], clob.getChars());
        }
    }
    
    public Object getObject(ExecutionContext ec, Object resultSet, int[] exprIndex)
    {
        try
        {
            // Check for null entries
            if (getDatastoreMapping(0).getObject(resultSet, exprIndex[0]) == null)
            {
                return null;
            }
        }
        catch (Exception e)
        {
            // Do nothing
        }

        String name = getDatastoreMapping(0).getString(resultSet, exprIndex[0]); 
        String mimeTypeBase = getDatastoreMapping(1).getString(resultSet, exprIndex[1]); 
        char[] chars = (char[]) getDatastoreMapping(2).getObject(resultSet,exprIndex[2]); 
        return new Clob(name, mimeTypeBase, chars);
    }
}
