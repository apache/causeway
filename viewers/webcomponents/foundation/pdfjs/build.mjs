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

import {createHash} from 'node:crypto';
import {chmodSync, cpSync, mkdirSync, readFileSync, readdirSync, rmSync, statSync, writeFileSync} from 'node:fs';
import {basename, relative, resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const directory = fileURLToPath(new URL('.', import.meta.url));
const packageRoot = resolve(directory, 'node_modules/pdfjs-dist');
const generated = resolve(directory, 'generated');
const assets = resolve(generated, 'assets');
const licenses = resolve(generated, 'licenses');
const policyPath = resolve(directory, 'policy.json');
const updatePolicy = process.argv.includes('--write-policy');

rmSync(generated, {recursive: true, force: true});
mkdirSync(assets, {recursive: true});
mkdirSync(licenses, {recursive: true});

for (const source of ['build/pdf.min.mjs', 'build/pdf.worker.min.mjs']) {
  cpSync(resolve(packageRoot, source), resolve(assets, basename(source)));
}
for (const source of ['cmaps', 'standard_fonts', 'iccs', 'wasm']) {
  cpSync(resolve(packageRoot, source), resolve(assets, source), {recursive: true});
}
cpSync(resolve(packageRoot, 'LICENSE'), resolve(licenses, 'pdfjs-dist-LICENSE.txt'));

const files = listFiles(assets).map(file => {
  chmodSync(file, 0o644);
  return {
    file: relative(assets, file).replaceAll('\\', '/'),
    bytes: statSync(file).size,
    sha256: sha256(readFileSync(file))
  };
});
const policy = JSON.parse(readFileSync(policyPath, 'utf8'));
const hashes = Object.fromEntries(files.map(file => [file.file, file.sha256]));
if (updatePolicy) {
  policy.files = hashes;
  writeFileSync(policyPath, `${JSON.stringify(policy, null, 2)}\n`);
} else if (JSON.stringify(policy.files) !== JSON.stringify(hashes)) {
  throw new Error('Generated PDF.js assets differ from policy. Review the pinned distribution and run npm run update-policy deliberately.');
}
const lock = JSON.parse(readFileSync(resolve(directory, 'package-lock.json'), 'utf8'));
writeFileSync(resolve(licenses, 'THIRD-PARTY.json'), `${JSON.stringify({
  package: 'pdfjs-dist',
  version: lock.packages['node_modules/pdfjs-dist'].version,
  license: 'Apache-2.0',
  source: 'https://github.com/mozilla/pdf.js',
  files
}, null, 2)}\n`);

function listFiles(root) {
  return readdirSync(root, {withFileTypes: true})
    .flatMap(entry => {
      const path = resolve(root, entry.name);
      return entry.isDirectory() ? listFiles(path) : [path];
    })
    .sort();
}

function sha256(bytes) {
  return createHash('sha256').update(bytes).digest('hex');
}
