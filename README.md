# Stratego

This repository contains the Spoofax implementations of the Stratego language. 

1. The `org.metaborg.meta.lang.stratego*` directories contain the old Stratego editor (_Stratego 1_ or _Stratego/XT 0.17_), which uses pre-compiled libraries from the _Stratego/XT_ project that can be found in the `strategoxt` repository of this organisation. 
2. The `stratego.lang*` and `stratego.test`/`stratego.example` directories contain the current _Stratego 2_ effort. 
3. The `stratego.build*` directories contain the Java code that glues together code in `stratego.lang` into the _Stratego 2_ incremental compiler.
4. The `_cellar` directory contains two (abandoned) experiments that were not clearly older copies of the above projects:
    1. The `stratego.typed*` directories contain an experiment to add more static analysis to _Stratego 1_ with the _NaBL2_ and _FlowSpec_ meta-languages of Spoofax.
    2. The `stratego`/`stratego-test` directories contain an experiment from the days of Spoofax 1, using an old version _NaBL_ to add static analysis. Plus some tests apparently. 
