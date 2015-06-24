package org.metaborg.meta.lang.stratego.strategies;

import org.strategoxt.lang.JavaInteropRegisterer;
import org.strategoxt.lang.Strategy;

public class InteropRegisterer extends JavaInteropRegisterer {
    public InteropRegisterer() {
        super(new Strategy[] { java_load_properties_0_0.instance, java_interpolate_environment_variables_0_0.instance });
    }
}
