package org.strategoxt.imp.editors.stratego.strategies;

import org.strategoxt.lang.JavaInteropRegisterer;
import org.strategoxt.lang.Strategy;

/**
 * Helper class for {@link java_strategy_0_0}.
 */
public class InteropRegisterer extends JavaInteropRegisterer {

  public InteropRegisterer() {
    super(new Strategy[] { 
    		java_load_properties_0_0.instance,
    		java_interpolate_environment_variables_0_0.instance
    		});
  }
}
