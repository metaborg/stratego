package org.strategoxt.imp.stratego_editor;

import java.io.InputStream;
import java.io.IOException;
import org.eclipse.imp.parser.IParseController;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.DescriptorFactory;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;

public class StrategoParseController extends DynamicParseController 
{ 
  private static final String LANGUAGE = "Stratego";

  private static final String TABLE = "/include/" + LANGUAGE + ".tbl";

  private static final String DESCRIPTOR = "/include/" + LANGUAGE + ".packed.esv";

  private static Descriptor descriptor;

  private static Throwable notLoadingCause;

  public static Descriptor getDescriptor()
  { 
    if(notLoadingCause != null)
      throw new RuntimeException(notLoadingCause);
    if(descriptor == null)
      createDescriptor();
    return descriptor;
  }

  private static void createDescriptor()
  { 
    try
    { 
      InputStream descriptorStream = StrategoParseController.class.getResourceAsStream(DESCRIPTOR);
      InputStream table = StrategoParseController.class.getResourceAsStream(TABLE);
      if(descriptorStream == null)
        throw new BadDescriptorException("Could not load descriptor file from " + DESCRIPTOR + " (not found in plugin)");
      if(table == null)
        throw new BadDescriptorException("Could not load parse table from " + TABLE + " (not found in plugin)");
      descriptor = DescriptorFactory.load(descriptorStream, table, null);
      descriptor.setAttachmentProvider(StrategoParseController.class);
    }
    catch(BadDescriptorException exc)
    { 
      notLoadingCause = exc;
      Environment.logException("Bad descriptor for " + LANGUAGE + " plugin", exc);
      throw new RuntimeException("Bad descriptor for " + LANGUAGE + " plugin", exc);
    }
    catch(IOException exc)
    { 
      notLoadingCause = exc;
      Environment.logException("I/O problem loading descriptor for " + LANGUAGE + " plugin", exc);
      throw new RuntimeException("I/O problem loading descriptor for " + LANGUAGE + " plugin", exc);
    }
  }

  @Override public IParseController getWrapped()
  { 
    if(!isInitialized())
    { 
      if(notLoadingCause != null)
        throw new RuntimeException(notLoadingCause);
      try
      { 
        initialize(getDescriptor().getLanguage());
      }
      catch(BadDescriptorException exc)
      { 
        notLoadingCause = exc;
        throw new RuntimeException(exc);
      }
    }
    return super.getWrapped();
  }

  @Override protected void setNotLoadingCause(Throwable value)
  { 
    notLoadingCause = value;
    super.setNotLoadingCause(value);
  }
}