package io.vedro;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyDecorator;
import com.jetbrains.python.psi.PyFunction;


public class VedroRunLineMarkerContributor extends RunLineMarkerContributor{
    protected static Icon ICON_RUN_SCENARIO = AllIcons.RunConfigurations.TestState.Run;
    protected static Icon ICON_RUN_PARAMS = AllIcons.RunConfigurations.TestState.Run_run;
    private static final String SCENARIO_BASE_CLASS = "vedro._scenario.Scenario";
    private static final String PARAMS_DECORATOR_NAME = "params";
    private static final String INIT_METHOD_NAME = "__init__";

    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        // Scenario class line marker
        if (element instanceof PyClass && isScenario((PyClass) element)) {
            return new Info(ICON_RUN_SCENARIO, ExecutorAction.getActions(), null);
        }
        // Params decorator line marker
        if (element instanceof PyDecorator && isParams((PyDecorator) element)) {
            return new Info(ICON_RUN_PARAMS, ExecutorAction.getActions(), null);
        }
        return null;
    }

    protected boolean isScenario(PyClass cls) {
        return cls.isSubclass(SCENARIO_BASE_CLASS, null);
    }

    protected boolean isParams(PyDecorator decorator) {
        String decoratorName = String.valueOf(decorator.getName());
        if (!decoratorName.equals(PARAMS_DECORATOR_NAME)) {
            return false;
        }

        PyFunction target = decorator.getTarget();
        if (target == null) {
            return false;
        }
        String targetName = target.getName();
        if (!targetName.equals(INIT_METHOD_NAME)) {
            return false;
        }

        PyClass containingClass = target.getContainingClass();
        return containingClass != null && isScenario(containingClass);
    }
}
