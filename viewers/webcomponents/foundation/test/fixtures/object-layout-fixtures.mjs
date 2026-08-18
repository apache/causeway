/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

export const GRID_NAMESPACES = `xmlns:bs="https://causeway.apache.org/applib/layout/grid/bootstrap3" xmlns:cpt="https://causeway.apache.org/applib/layout/component"`;

export const COMPLETE_OBJECT_GRID = `<?xml version="1.0" encoding="UTF-8"?>
<bs:grid ${GRID_NAMESPACES}>
  <bs:row>
    <bs:col span="12" unreferencedActions="true">
      <cpt:domainObject/>
      <cpt:action id="changeName"><cpt:named>Rename department</cpt:named></cpt:action>
    </bs:col>
  </bs:row>
  <bs:row>
    <bs:col span="4">
      <cpt:fieldSet id="identity" name="Identity" unreferencedProperties="true">
        <cpt:property id="name"/>
        <cpt:property id="code"/>
      </cpt:fieldSet>
    </bs:col>
    <bs:col span="8">
      <bs:tabGroup unreferencedCollections="true">
        <bs:tab name="Current staff">
          <cpt:collection id="staffMembers"/>
        </bs:tab>
      </bs:tabGroup>
    </bs:col>
  </bs:row>
</bs:grid>`;

export const PARTIAL_OBJECT_GRID = `<bs:grid ${GRID_NAMESPACES}>
  <bs:row>
    <bs:col span="25" experimental="ignored">
      <cpt:domainObject/>
      <bs:unsupported>
        <cpt:fieldSet id="partial" name="Partial">
          <cpt:property id="name"/>
          <cpt:property id="missing"/>
          <cpt:collection id="code"/>
          <cpt:property id="name"/>
        </cpt:fieldSet>
      </bs:unsupported>
    </bs:col>
  </bs:row>
</bs:grid>`;

export const UNREFERENCED_BEFORE_EXPLICIT_GRID = `<bs:grid ${GRID_NAMESPACES}>
  <bs:row>
    <bs:col span="6" unreferencedProperties="true"/>
    <bs:col span="6"><cpt:property id="name"/></bs:col>
  </bs:row>
</bs:grid>`;

export const UNSUPPORTED_ONLY_GRID = `<bs:grid ${GRID_NAMESPACES}><bs:row><bs:col span="12"><bs:carousel/></bs:col></bs:row></bs:grid>`;

export const MALFORMED_GRIDS = Object.freeze([
  '<bs:grid><bs:row></bs:grid>',
  '<!DOCTYPE grid [<!ENTITY secret SYSTEM "file:///etc/passwd">]><bs:grid>&secret;</bs:grid>',
  `<bs:grid ${GRID_NAMESPACES}><script>alert(1)</script></bs:grid>`,
  `<bs:grid ${GRID_NAMESPACES} onclick="alert(1)"/>`,
  `<bs:grid ${GRID_NAMESPACES}>&unknown;</bs:grid>`,
  `<?xml version="1.0"?><?unsafe value?><bs:grid ${GRID_NAMESPACES}/>`
]);

export function objectLayoutMembers() {
  return new Map([
    ['name', {id: 'name', kind: 'property'}],
    ['code', {id: 'code', kind: 'property'}],
    ['status', {id: 'status', kind: 'property'}],
    ['notes', {id: 'notes', kind: 'property'}],
    ['staffMembers', {id: 'staffMembers', kind: 'collection'}],
    ['formerStaff', {id: 'formerStaff', kind: 'collection'}],
    ['changeName', {id: 'changeName', kind: 'action'}]
  ]);
}
