package mb.stratego.build.strincr;

import java.util.Collections;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.stratego.build.util.TermWithLastModified;

public class Lib implements TaskDef<Front.Input, ModuleData> {
    public static final String id = Lib.class.getCanonicalName();

    @Override public ModuleData exec(ExecContext context, Front.Input input) throws Exception {
        final TermWithLastModified ast = input.moduleImportService.getModuleAst(input.moduleIdentifier);
        return new ModuleData(input.moduleIdentifier, ast, Collections.emptyList(), Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap());
    }

    @Override public String getId() {
        return id;
    }
}
