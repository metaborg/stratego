package org.strategoxt.imp.editors.stratego.strategies;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class java_load_properties_0_0 extends Strategy {

	public static java_load_properties_0_0 instance = new java_load_properties_0_0();

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm current) {

		if (current.getTermType() != IStrategoTerm.STRING)
			return null;

		IStrategoString path = (IStrategoString) current;

		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(path.stringValue()));
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		ITermFactory factory = context.getFactory();
		IStrategoList els = factory.makeList();
		for (Object o : Collections.list(prop.propertyNames())) {
			if(!(o instanceof String))
				continue;
			String key = (String)o;
			IStrategoString k = factory.makeString(key);
			IStrategoString v = factory.makeString(prop.getProperty(key));
			IStrategoTuple tup = factory.makeTuple(k, v);
			els = factory.makeListCons(tup, els);
		}

		return els;
	}

}
