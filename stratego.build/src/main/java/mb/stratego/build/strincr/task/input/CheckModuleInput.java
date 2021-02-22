package mb.stratego.build.strincr.task.input;

import javax.annotation.Nullable;

import mb.stratego.build.strincr.IModuleImportService;

public class CheckModuleInput extends FrontInput {
    public final IModuleImportService.ModuleIdentifier mainModuleIdentifier;

    public CheckModuleInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        IModuleImportService.ModuleIdentifier moduleIdentifier,
        IModuleImportService moduleImportService) {
        super(moduleIdentifier, moduleImportService);
        this.mainModuleIdentifier = mainModuleIdentifier;
    }

    public ResolveInput resolveInput() {
        return new ResolveInput(mainModuleIdentifier, moduleImportService);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        CheckModuleInput input = (CheckModuleInput) o;

        return mainModuleIdentifier.equals(input.mainModuleIdentifier);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mainModuleIdentifier.hashCode();
        return result;
    }

    @Override public String toString() {
        return "CheckModule.Input(" + moduleIdentifier + ")";
    }
}
