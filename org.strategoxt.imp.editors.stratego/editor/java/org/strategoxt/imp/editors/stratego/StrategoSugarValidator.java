package org.strategoxt.imp.editors.stratego;

import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.services.MetaFileLanguageValidator;

public class StrategoSugarValidator extends MetaFileLanguageValidator 
{ 
  @Override public Descriptor getDescriptor()
  { 
    return StrategoSugarParseController.getDescriptor();
  }
}