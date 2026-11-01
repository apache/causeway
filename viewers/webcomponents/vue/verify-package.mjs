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

import {execFileSync} from 'node:child_process';
import {mkdtempSync, readFileSync, rmSync, writeFileSync} from 'node:fs';
import {tmpdir} from 'node:os';
import {basename, join, resolve} from 'node:path';

const packed = JSON.parse(execFileSync('npm', ['pack', '--json'], {encoding: 'utf8'}))[0];
const names = new Set(packed.files.map(file => file.path));
for (const required of ['dist/causeway-vue.js', 'dist/index.d.ts', 'dist/causeway-vue.css', 'README.adoc', 'LICENSE', 'NOTICE']) {
  if (!names.has(required)) throw new Error(`Packed Vue viewer is missing ${required}.`);
}
if ([...names].some(name => name.startsWith('src/') || name.startsWith('test/') || name === 'vite.config.ts')) {
  throw new Error('Packed Vue viewer contains private source files.');
}

const directory = mkdtempSync(join(tmpdir(), 'causeway-vue-consumer-'));
const tarball = resolve(packed.filename);
try {
  writeFileSync(join(directory, 'package.json'), JSON.stringify({type: 'module', private: true}, null, 2));
  execFileSync('npm', ['install', '--ignore-scripts', '--no-audit', '--no-fund', tarball], {cwd: directory, stdio: 'ignore'});
  const exports = execFileSync('node', ['--input-type=module', '--eval', "import * as viewer from '@apache-causeway/vue-viewer'; console.log(Object.keys(viewer).length)"], {cwd: directory, encoding: 'utf8'}).trim();
  if (!Number.isFinite(Number(exports)) || Number(exports) < 10) throw new Error('Packed Vue viewer exports are incomplete.');
  const manifest = JSON.parse(readFileSync(join(directory, 'node_modules/@apache-causeway/vue-viewer/package.json'), 'utf8'));
  if (!manifest.peerDependencies?.vue || !manifest.peerDependencies?.['vue-router']) throw new Error('Packed peer dependencies are incomplete.');
  console.log(`Verified ${basename(tarball)} with ${packed.files.length} public files and ${exports} exports.`);
} finally {
  rmSync(directory, {recursive: true, force: true});
  rmSync(tarball, {force: true});
}
