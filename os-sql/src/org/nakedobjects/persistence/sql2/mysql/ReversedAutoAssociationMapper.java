package org.nakedobjects.persistence.sql2.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.ResolveException;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.persistence.sql2.CollectionMapper;
import org.nakedobjects.persistence.sql2.SqlOid;
import org.nakedobjects.utility.ConfigurationException;


/** used where there is a one to many association, and the elements are only known to parent */
public class ReversedAutoAssociationMapper extends AbstractAutoMapper implements CollectionMapper {
	private static final Logger LOG = Logger.getLogger(ReversedAutoAssociationMapper.class);
	private String table;
	private String parentColumn;
	private String elementIdColumn;
	private Field field;
	
	public ReversedAutoAssociationMapper(String elemenType, Field field, String parameterBase) throws ConfigurationException, ObjectStoreException {
		super(elemenType, parameterBase);
		
		this.field = field;

		// TODO load in properties
		String className = nakedClass.getShortName().toLowerCase();

		parentColumn = "FK" + className;
	
	
		table = fieldMapper.getColumnName(field.getName());
		
		String columnName = fieldMapper.getColumnName(field.getName());
		elementIdColumn = "PK" + columnName;

		if(nakedClass.fullName().startsWith("org.nakedobjects.")) {
			table = "no_" + table;
		}
	}

	public void loadInternalCollection(NakedObject parent)
			throws ResolveException, SQLException {
		InternalCollection collection = (InternalCollection) field.get(parent);
		LOG.debug("Loading internal collection " + collection);
		long parentId = primaryKey(parent.getOid());
		
		String statement = "select " + elementIdColumn + "," + columnList() + " from " + table + " where "
				+ parentColumn + " = " + parentId;
		ResultSet rs = db.select(statement);
		while (rs.next()) {
			int id = rs.getInt(elementIdColumn);
			NakedObject element = loadObject(nakedClass, new SqlOid(id, nakedClass.fullName()));
			LOG.debug("  element  " + element);
			collection.added(element);
		}
		collection.setResolved();
	}

	public void saveInternalCollection(NakedObject parent) throws ObjectStoreException {
		InternalCollection collection = (InternalCollection) field.get(parent);
		LOG.debug("Saving internal collection " + collection);
		long parentId = primaryKey(parent.getOid());
		
		db.update("delete from " + table + " where " + parentColumn + " = " + parentId);
		
		String columns = parentColumn + ", " + elementIdColumn;
		int size = collection.size();
		for (int i = 0; i < size; i++) {
			NakedObject element = collection.elementAt(i);
			
			long elementId = primaryKey(element.getOid());
			String cls = element.getNakedClass().fullName();
			String values = parentId + "," + elementId + ", '" + cls + "'";
			String statement = "insert into " + table + " (" + columns + ") values (" + values + ")";
			db.update(statement);
		}
	}

}