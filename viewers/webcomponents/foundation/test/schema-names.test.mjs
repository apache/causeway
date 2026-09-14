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

import assert from 'node:assert/strict';
import test from 'node:test';
import {RichSchemaNameError, RichSchemaNames, sanitizeGraphQLName} from '../src/schema-names.mjs';

const names = new RichSchemaNames();

test('maps Causeway logical names using the rich schema grammar', () => {
  assert.equal(sanitizeGraphQLName('university.dept.Department'), 'university_dept_Department');
  assert.equal(sanitizeGraphQLName('university.calc.calculator-hyphenated'), 'university_calc_calculatorHyphenated');
  assert.equal(sanitizeGraphQLName('example.Type#action()'), 'example_Type__action');
  assert.equal(names.objectType('university.dept.Department'), 'rich__university_dept_Department');
  assert.equal(names.objectInputType('university.dept.Department'), 'rich__university_dept_Department__gqlv_input');
  assert.equal(names.objectField('university.dept.Department'), 'university_dept_Department');
});

test('supports configured lookup field grammar', () => {
  const configured = new RichSchemaNames({
    richRootField: '',
    lookupArgumentName: 'target',
    objectFieldPrefix: 'lookup_',
    objectFieldSuffix: '_object'
  });
  assert.equal(configured.richRootField, null);
  assert.equal(configured.lookupArgumentName, 'target');
  assert.equal(configured.objectField('university.dept.Department'), 'lookup_university_dept_Department_object');
});

test('classifies generated rich member and metadata types', () => {
  const owner = names.objectType('university.dept.Department');
  assert.equal(names.classify(`${owner}__name__gqlv_property`, owner).kind, 'property');
  assert.equal(names.classify(`${owner}__staff__gqlv_collection`, owner).kind, 'collection');
  assert.equal(names.classify(`${owner}__changeName__gqlv_action`, owner).kind, 'action');
  assert.equal(names.classify(`${owner}__changeName__newName__gqlv_action_parameter`, owner).kind, 'parameter');
  assert.equal(names.classify(`${owner}__gqlv_meta`, owner).kind, 'metadata');
});

test('reports invalid and unrecognized names with diagnostics', () => {
  assert.throws(() => names.objectField('invalid name'), RichSchemaNameError);
  assert.throws(
    () => names.classify('simple__university_dept_Department'),
    error => error.code === 'UNRECOGNIZED_GENERATED_NAME'
  );
});
