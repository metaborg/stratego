package org.strategoxt.imp.editors.stratego.strategies;

import java.util.Properties;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class java_interpolate_environment_variables_0_0 extends Strategy {

	public static java_interpolate_environment_variables_0_0 instance = new java_interpolate_environment_variables_0_0();

	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm current) {
		
		if(current.getTermType() != IStrategoTerm.STRING)
			return null;

		Properties props = System.getProperties();
		String text = ((IStrategoString)current).stringValue();
		StringBuffer res = new StringBuffer();

		int cursor = 0;
		
		for(int i = 0; i < text.length()-2; i++) {
			if(text.charAt(i) == '$' && text.charAt(i + 1) == '{') {
				int start = i;
				int end = -1;
				for(int j = start; j < text.length(); j++)
					if(text.charAt(j) == '}') {
						end = j;
						break;
					}
				if(end > start) {
					res.append(text.substring(cursor, start));
					String key = text.substring(start + 2, end);
					if(props.containsKey(key)) 
						res.append(props.getProperty(key));
					else if("HOME".equals(key)) {
						res.append(props.getProperty("user.home"));
					}
					cursor = end + 1;
				}
			}
		}
		
		res.append(text.substring(cursor));
		
		return context.getFactory().makeString(res.toString());
	}

}
