/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {build} from 'esbuild';
import {createHash} from 'node:crypto';
import {cpSync, existsSync, mkdirSync, readFileSync, readdirSync, rmSync, statSync, writeFileSync} from 'node:fs';
import {basename, dirname, resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {gzipSync} from 'node:zlib';

const directory = fileURLToPath(new URL('.', import.meta.url));
const generated = resolve(directory, 'generated');
const assets = resolve(generated, 'assets');
const licenses = resolve(generated, 'licenses');
const bundlePath = resolve(assets, 'vaadin-grid.js');
const policyPath = resolve(directory, 'policy.json');
const lock = JSON.parse(readFileSync(resolve(directory, 'package-lock.json'), 'utf8'));
const updatePolicy = process.argv.includes('--write-policy');

rmSync(generated, {recursive: true, force: true});
mkdirSync(assets, {recursive: true});
mkdirSync(licenses, {recursive: true});

await build({
  entryPoints: [resolve(directory, 'grid-entry.mjs')],
  outfile: bundlePath,
  bundle: true,
  format: 'esm',
  platform: 'browser',
  target: ['es2022'],
  minify: true,
  legalComments: 'none',
  sourcemap: false,
  define: {'process.env.NODE_ENV': '"production"'},
  plugins: [{
    name: 'disable-vaadin-usage-statistics',
    setup(pluginBuild) {
      pluginBuild.onResolve({filter: /^@vaadin\/vaadin-usage-statistics\/vaadin-usage-statistics\.js$/}, () => ({
        path: resolve(directory, 'node_modules/@vaadin/vaadin-usage-statistics/vaadin-usage-statistics-optout.js')
      }));
    }
  }]
});

const packages = productionPackages(lock);
for (const pkg of packages) copyLegalFiles(pkg);
const bundle = readFileSync(bundlePath);
const bundleSha256 = sha256(bundle);
const bundleGzipBytes = gzipSync(bundle, {level: 9}).length;
const policy = JSON.parse(readFileSync(policyPath, 'utf8'));
if (updatePolicy) {
  policy.bundleSha256 = bundleSha256;
  writeFileSync(policyPath, `${JSON.stringify(policy, null, 2)}\n`);
} else if (policy.bundleSha256 !== bundleSha256) {
  throw new Error(`Generated Grid bundle checksum ${bundleSha256} does not match policy ${policy.bundleSha256}. Review the closure and run npm run update-policy deliberately.`);
}
const metadata = {
  bundle: {file: 'vaadin-grid.js', rawBytes: bundle.length, gzipBytes: bundleGzipBytes, sha256: bundleSha256},
  entryPoints: policy.entryPoints,
  usageStatisticsAlias: '@vaadin/vaadin-usage-statistics/vaadin-usage-statistics-optout.js',
  cspStyleHashes: policy.cspStyleHashes,
  packages
};
writeFileSync(resolve(assets, 'csp-policy.json'), `${JSON.stringify({styleHashes: policy.cspStyleHashes}, null, 2)}\n`);
writeFileSync(resolve(licenses, 'THIRD-PARTY.json'), `${JSON.stringify(metadata, null, 2)}\n`);
console.log(JSON.stringify({bundle: metadata.bundle, packageCount: packages.length, styleHashCount: policy.cspStyleHashes.length}, null, 2));

function productionPackages(packageLock) {
  const result = [];
  for (const [packagePath, entry] of Object.entries(packageLock.packages ?? {})) {
    if (!packagePath.startsWith('node_modules/') || entry.dev) continue;
    const name = packagePath.slice('node_modules/'.length);
    const packageJsonPath = resolve(directory, packagePath, 'package.json');
    if (!existsSync(packageJsonPath)) throw new Error(`Missing installed package ${name}. Run npm ci --ignore-scripts.`);
    const manifest = JSON.parse(readFileSync(packageJsonPath, 'utf8'));
    const license = manifest.license ?? entry.license;
    if (!license) throw new Error(`Package ${name} has no declared license.`);
    result.push({
      name,
      version: manifest.version,
      license,
      integrity: entry.integrity ?? null,
      repository: typeof manifest.repository === 'string' ? manifest.repository : manifest.repository?.url ?? null
    });
  }
  return result.sort((left, right) => left.name.localeCompare(right.name));
}

function copyLegalFiles(pkg) {
  const packageDirectory = resolve(directory, 'node_modules', pkg.name);
  let candidates = readdirSync(packageDirectory).filter(file => /^(LICENSE|LICENCE|NOTICE)(\.|$)/i.test(file));
  let sourceDirectory = packageDirectory;
  if (!candidates.some(file => /^LICEN[CS]E(\.|$)/i.test(file)) && pkg.name === '@lit-labs/ssr-dom-shim') {
    sourceDirectory = resolve(directory, 'node_modules/lit');
    candidates = readdirSync(sourceDirectory).filter(file => /^LICEN[CS]E(\.|$)/i.test(file));
    pkg.licenseSource = 'lit package from the same repository and BSD-3-Clause license';
  }
  if (!candidates.some(file => /^LICEN[CS]E(\.|$)/i.test(file))) throw new Error(`Package ${pkg.name} has no distributable license file.`);
  for (const file of candidates) {
    const suffix = sourceDirectory === packageDirectory ? file : `${file}-from-lit`;
    const target = `${sanitize(pkg.name)}-${pkg.version}-${suffix}`;
    cpSync(resolve(sourceDirectory, file), resolve(licenses, target));
  }
}

function sanitize(value) {
  return value.replace(/^@/, '').replaceAll('/', '__');
}

function sha256(value) {
  return createHash('sha256').update(value).digest('hex');
}
