# Stratego
[![Build][github-badge:build]][github:build]
[![License][license-badge]][license]
[![GitHub Release][github-badge:release]][github:release]

This repository contains the Spoofax implementations of the Stratego language.

1. The `org.metaborg.meta.lang.stratego*` directories contain the old Stratego editor (_Stratego 1_ or _Stratego/XT 0.17_), which uses pre-compiled libraries from the _Stratego/XT_ project that can be found in the `strategoxt` repository of this organisation.
2. The `stratego.lang*` and `stratego.test`/`stratego.example` directories contain the current _Stratego 2_ effort.
3. The `stratego.build*` directories contain the Java code that glues together code in `stratego.lang` into the _Stratego 2_ incremental compiler.
4. The `_cellar` directory contains two (abandoned) experiments that were not clearly older copies of the above projects:
   1. The `stratego.typed*` directories contain an experiment to add more static analysis to _Stratego 1_ with the _NaBL2_ and _FlowSpec_ meta-languages of Spoofax.
   2. The `stratego`/`stratego-test` directories contain an experiment from the days of Spoofax 1, using an old version _NaBL_ to add static analysis. Plus some tests apparently.


## License
Copyright 2016-2024 [Programming Languages Group](https://pl.ewi.tudelft.nl/), [Delft University of Technology](https://www.tudelft.nl/)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at <https://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an **"as is" basis, without warranties or conditions of any kind**, either express or implied. See the License for the specific language governing permissions and limitations under the License.



[github-badge:build]: https://img.shields.io/github/actions/workflow/status/metaborg/stratego/build.yaml
[github:build]: https://github.com/metaborg/stratego/actions
[license-badge]: https://img.shields.io/github/license/metaborg/stratego
[license]: https://github.com/metaborg/stratego/blob/main/LICENSE
[github-badge:release]: https://img.shields.io/github/v/release/metaborg/stratego?display_name=release
[github:release]: https://github.com/metaborg/stratego/releases
