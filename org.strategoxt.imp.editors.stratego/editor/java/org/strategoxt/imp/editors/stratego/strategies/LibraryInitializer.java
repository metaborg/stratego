package org.strategoxt.imp.editors.stratego.strategies;

import java.util.Arrays;
import java.util.List;

import org.strategoxt.lang.Context;
import org.strategoxt.lang.RegisteringStrategy;

public class LibraryInitializer extends org.strategoxt.lang.LibraryInitializer {

	@Override
	protected List<RegisteringStrategy> getLibraryStrategies() {
		return Arrays.asList(java_interpolate_environment_variables_0_0.instance, java_load_properties_0_0.instance);
	}

	@Override
	protected void initializeLibrary(Context context) {
		Main.init(context);
	}

}
