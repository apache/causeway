package org.nakedobjects.persistence.sql2.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.ResolveException;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.persistence.sql2.CollectionMapper;
import org.nakedobjects.persistence.sql2.SqlOid;

public class AutoAssociationMapper implements CollectionMapper {
	private static final Logger LOG = Logger.getLogger(AutoAssociationMapper.class);
	private String table;
	private String parentColumn;
	private String elementIdColumn;
	private String elementClassColumn;
	private Field field;
	private AbstractAutoMapper mapper;

	public AutoAssociationMapper(AbstractAutoMapper mapper, NakedClass nakedClass, Field field) throws ObjectStoreException {
		this.mapper = mapper;
		this.field = field;

		// TODO load in properties
		String className = nakedClass.getShortName().toLowerCase();

		parentColumn = "FK" + className;
	
		String columnName = mapper.fieldMapper.getColumnName(field.getName());
		elementIdColumn = "PK" + columnName;
	
		elementClassColumn = columnName + "Class";

		table = className + "_" + columnName;
		if(nakedClass.fullName().startsWith("org.nakedobjects.")) {
			table = "no_" + table;
		}
	}

	public boolean needsTables() throws ObjectStoreException {
		return ! mapper.db.hasTable(table);
	}
	
	public void createTables() throws ObjectStoreException {
		// TODO load in properties

		String columns = parentColumn + " int, " + elementClassColumn + " varchar(255), " + elementIdColumn + " int";
		mapper.db.update("create table " + table + " (" + columns + ")");
	}

	public void loadInternalCollection(NakedObject parent)
			throws ResolveException, SQLException {
		InternalCollection collection = (InternalCollection) field.get(parent);
		LOG.debug("Loading internal collection " + collection);
		long parentId = mapper.primaryKey(parent.getOid());
		
		String statement = "select " + elementIdColumn + "," + elementClassColumn + " from " + table + " where "
				+ parentColumn + " = " + parentId;
		ResultSet rs = mapper.db.select(statement);
		while (rs.next()) {
			int id = rs.getInt(elementIdColumn);
			String cls = rs.getString(elementClassColumn);
			NakedClass elementCls = NakedClassManager.getInstance().getNakedClass(cls);
			NakedObject element = mapper.loadObject(elementCls, new SqlOid(id, cls));
			LOG.debug("  element  " + element);
			collection.added(element);
		}
		collection.setResolved();
	}

	public void saveInternalCollection(NakedObject parent) throws ObjectStoreException {
		InternalCollection collection = (InternalCollection) field.get(parent);
		LOG.debug("Saving internal collection " + collection);
		long parentId = mapper.primaryKey(parent.getOid());
		
		mapper.db.update("delete from " + table + " where " + parentColumn + " = " + parentId);
		
		String columns = parentColumn + ", " + elementIdColumn + ", " + elementClassColumn;
		int size = collection.size();
		for (int i = 0; i < size; i++) {
			NakedObject element = collection.elementAt(i);
			
			long elementId = mapper.primaryKey(element.getOid());
			String cls = element.getNakedClass().fullName();
			String values = parentId + "," + elementId + ", '" + cls + "'";
			String statement = "insert into " + table + " (" + columns + ") values (" + values + ")";
			mapper.db.update(statement);
		}
	}

}