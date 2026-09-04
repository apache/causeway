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
import {existsSync, readFileSync, readdirSync, statSync} from 'node:fs';
import {relative, resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const directory = fileURLToPath(new URL('.', import.meta.url));
const assets = resolve(directory, 'generated/assets');
const licenses = resolve(directory, 'generated/licenses');
const policy = JSON.parse(readFileSync(resolve(directory, 'policy.json'), 'utf8'));
const lock = JSON.parse(readFileSync(resolve(directory, 'package-lock.json'), 'utf8'));
const dependency = lock.packages?.['node_modules/pdfjs-dist'];
if (dependency?.version !== policy.version || !dependency.integrity) {
  throw new Error(`Pinned pdfjs-dist mismatch: ${dependency?.version ?? 'missing'}.`);
}
if (dependency.license !== 'Apache-2.0') {
  throw new Error(`Unexpected pdfjs-dist license: ${dependency.license ?? 'missing'}.`);
}
if (dependency.engines?.node !== policy.nodeEngine) {
  throw new Error(`Unexpected pdfjs-dist Node.js engine: ${dependency.engines?.node ?? 'missing'}.`);
}
for (const required of ['pdf.min.mjs', 'pdf.worker.min.mjs']) {
  if (!existsSync(resolve(assets, required))) throw new Error(`Required PDF.js asset is missing: ${required}.`);
}
for (const required of ['cmaps', 'standard_fonts', 'iccs', 'wasm']) {
  if (!existsSync(resolve(assets, required))) throw new Error(`Required PDF.js support directory is missing: ${required}.`);
}
for (const required of ['pdfjs-dist-LICENSE.txt', 'THIRD-PARTY.json']) {
  if (!existsSync(resolve(licenses, required))) throw new Error(`Generated PDF.js legal material is missing: ${required}.`);
}
const files = listFiles(assets);
const actual = Object.fromEntries(files.map(file => [
  relative(assets, file).replaceAll('\\', '/'),
  createHash('sha256').update(readFileSync(file)).digest('hex')
]));
if (JSON.stringify(actual) !== JSON.stringify(policy.files)) {
  throw new Error('Generated PDF.js asset inventory or checksums differ from policy.');
}
const totalBytes = files.reduce((sum, file) => sum + statSync(file).size, 0);
if (totalBytes > policy.maximumBytes) {
  throw new Error(`PDF.js assets use ${totalBytes} bytes, exceeding ${policy.maximumBytes}.`);
}
const metadata = JSON.parse(readFileSync(resolve(licenses, 'THIRD-PARTY.json'), 'utf8'));
if (metadata.version !== policy.version || metadata.license !== 'Apache-2.0') {
  throw new Error('Generated PDF.js third-party metadata differs from policy.');
}
const moduleText = `${readFileSync(resolve(assets, 'pdf.min.mjs'), 'utf8')}\n${readFileSync(resolve(assets, 'pdf.worker.min.mjs'), 'utf8')}`;
for (const marker of ['cdnjs.cloudflare.com', 'unpkg.com', 'jsdelivr.net']) {
  if (moduleText.includes(marker)) throw new Error(`Remote runtime dependency found: ${marker}.`);
}
console.log(`Verified pdfjs-dist ${policy.version}: ${files.length} assets, ${totalBytes} bytes.`);

function listFiles(root) {
  return readdirSync(root, {withFileTypes: true})
    .flatMap(entry => {
      const path = resolve(root, entry.name);
      return entry.isDirectory() ? listFiles(path) : [path];
    })
    .sort();
}
