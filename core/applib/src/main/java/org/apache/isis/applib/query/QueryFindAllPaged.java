/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.query;


/**
 * Used to support paging access. When paging through a dataset, use this method
 * to access portions of the dataset at a time.
 *
 * @author Kevin Meyer
 * @version $Rev$ $Date$
 */
public class QueryFindAllPaged<T> extends QueryBuiltInAbstract<T> {

    private static final long serialVersionUID = 1L;
    private final long start;
    private final long count;
    
    public QueryFindAllPaged(final Class<T> type, final long start, final long count) {
        super(type);
        this.start = start;
        this.count = count;
    }

    public QueryFindAllPaged(final String typeName, final long start, final long count) {
        super(typeName);
        this.start = start;
        this.count = count;
    }
    

    @Override
    public String getDescription() {
        return getResultTypeName() + String.format(" (all instances, paged %d %d)", getStart(), getCount());
    }


    /**
     * The start index into the set table
     * @return
     */
    public long getStart() {
        return start;
    }


    /**
     * The number of items to return, starting at {@link QueryFindAllPaged#getStart()}
     * @return
     */
    public long getCount() {
        return count;
    }
}
