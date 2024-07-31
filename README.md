<!--
!! THIS FILE WAS GENERATED USING repoman !!
Modify `repo.yaml` instead and use `repoman` to update this file
See: https://github.com/metaborg/metaborg-gradle/
-->

# Stratego
[![Build][github-badge:build]][github:build]
[![License][license-badge]][license]
[![GitHub Release][github-badge:release]][github:release]

The Spoofax implementations of the Stratego language.


| Language | Latest Release | Latest Snapshot |
|----------|----------------|-----------------|
| `org.metaborg.devenv:gpp` | [![Release][mvn-rel-badge:org.metaborg.devenv:gpp]][mvn:org.metaborg.devenv:gpp] | [![Snapshot][mvn-snap-badge:org.metaborg.devenv:gpp]][mvn:org.metaborg.devenv:gpp] |
| `org.metaborg.devenv:org.metaborg.meta.lang.stratego` | [![Release][mvn-rel-badge:org.metaborg.devenv:org.metaborg.meta.lang.stratego]][mvn:org.metaborg.devenv:org.metaborg.meta.lang.stratego] | [![Snapshot][mvn-snap-badge:org.metaborg.devenv:org.metaborg.meta.lang.stratego]][mvn:org.metaborg.devenv:org.metaborg.meta.lang.stratego] |
| `org.metaborg.devenv:stratego.lang` | [![Release][mvn-rel-badge:org.metaborg.devenv:stratego.lang]][mvn:org.metaborg.devenv:stratego.lang] | [![Snapshot][mvn-snap-badge:org.metaborg.devenv:stratego.lang]][mvn:org.metaborg.devenv:stratego.lang] |
| `org.metaborg.devenv:strategolib` | [![Release][mvn-rel-badge:org.metaborg.devenv:strategolib]][mvn:org.metaborg.devenv:strategolib] | [![Snapshot][mvn-snap-badge:org.metaborg.devenv:strategolib]][mvn:org.metaborg.devenv:strategolib] |

| Artifact | Latest Release | Latest Snapshot |
|----------|----------------|-----------------|
| `org.metaborg.devenv:stratego.build` | [![Release][mvn-rel-badge:org.metaborg.devenv:stratego.build]][mvn:org.metaborg.devenv:stratego.build] | [![Snapshot][mvn-snap-badge:org.metaborg.devenv:stratego.build]][mvn:org.metaborg.devenv:stratego.build] |
| `org.metaborg.devenv:stratego.build.spoofax2` | [![Release][mvn-rel-badge:org.metaborg.devenv:stratego.build.spoofax2]][mvn:org.metaborg.devenv:stratego.build.spoofax2] | [![Snapshot][mvn-snap-badge:org.metaborg.devenv:stratego.build.spoofax2]][mvn:org.metaborg.devenv:stratego.build.spoofax2] |


1. The `org.metaborg.meta.lang.stratego*` directories contain the old Stratego editor (_Stratego 1_ or _Stratego/XT 0.17_), which uses pre-compiled libraries from the _Stratego/XT_ project that can be found in the `strategoxt` repository of this organisation.
2. The `stratego.lang*` and `stratego.test`/`stratego.example` directories contain the current _Stratego 2_ effort.
3. The `stratego.build*` directories contain the Java code that glues together code in `stratego.lang` into the _Stratego 2_ incremental compiler.
4. The `_cellar` directory contains two (abandoned) experiments that were not clearly older copies of the above projects:
   1. The `stratego.typed*` directories contain an experiment to add more static analysis to _Stratego 1_ with the _NaBL2_ and _FlowSpec_ meta-languages of Spoofax.
   2. The `stratego`/`stratego-test` directories contain an experiment from the days of Spoofax 1, using an old version _NaBL_ to add static analysis. Plus some tests apparently.


## License
Copyright 2007-2024 [Programming Languages Group](https://pl.ewi.tudelft.nl/), [Delft University of Technology](https://www.tudelft.nl/)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at <https://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an **"as is" basis, without warranties or conditions of any kind**, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[github-badge:build]: https://img.shields.io/github/actions/workflow/status/metaborg/stratego/build.yaml
[github:build]: https://github.com/metaborg/stratego/actions
[license-badge]: https://img.shields.io/github/license/metaborg/stratego
[license]: https://github.com/metaborg/stratego/blob/master/LICENSE.md
[github-badge:release]: https://img.shields.io/github/v/release/metaborg/stratego?display_name=release
[github:release]: https://github.com/metaborg/stratego/releases

[mvn:org.metaborg.devenv:gpp]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg.devenv~gpp~~~
[mvn-rel-badge:org.metaborg.devenv:gpp]: https://img.shields.io/nexus/r/org.metaborg.devenv/gpp?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg.devenv:gpp]: https://img.shields.io/nexus/s/org.metaborg.devenv/gpp?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn:org.metaborg.devenv:org.metaborg.meta.lang.stratego]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg.devenv~org.metaborg.meta.lang.stratego~~~
[mvn-rel-badge:org.metaborg.devenv:org.metaborg.meta.lang.stratego]: https://img.shields.io/nexus/r/org.metaborg.devenv/org.metaborg.meta.lang.stratego?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg.devenv:org.metaborg.meta.lang.stratego]: https://img.shields.io/nexus/s/org.metaborg.devenv/org.metaborg.meta.lang.stratego?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn:org.metaborg.devenv:stratego.lang]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg.devenv~stratego.lang~~~
[mvn-rel-badge:org.metaborg.devenv:stratego.lang]: https://img.shields.io/nexus/r/org.metaborg.devenv/stratego.lang?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg.devenv:stratego.lang]: https://img.shields.io/nexus/s/org.metaborg.devenv/stratego.lang?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn:org.metaborg.devenv:strategolib]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg.devenv~strategolib~~~
[mvn-rel-badge:org.metaborg.devenv:strategolib]: https://img.shields.io/nexus/r/org.metaborg.devenv/strategolib?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg.devenv:strategolib]: https://img.shields.io/nexus/s/org.metaborg.devenv/strategolib?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn:org.metaborg.devenv:stratego.build]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg.devenv~stratego.build~~~
[mvn-rel-badge:org.metaborg.devenv:stratego.build]: https://img.shields.io/nexus/r/org.metaborg.devenv/stratego.build?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg.devenv:stratego.build]: https://img.shields.io/nexus/s/org.metaborg.devenv/stratego.build?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn:org.metaborg.devenv:stratego.build.spoofax2]: https://artifacts.metaborg.org/#nexus-search;gav~org.metaborg.devenv~stratego.build.spoofax2~~~
[mvn-rel-badge:org.metaborg.devenv:stratego.build.spoofax2]: https://img.shields.io/nexus/r/org.metaborg.devenv/stratego.build.spoofax2?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
[mvn-snap-badge:org.metaborg.devenv:stratego.build.spoofax2]: https://img.shields.io/nexus/s/org.metaborg.devenv/stratego.build.spoofax2?server=https%3A%2F%2Fartifacts.metaborg.org&label=%20
