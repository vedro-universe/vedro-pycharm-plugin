package io.vedro;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyDecorator;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class VedroRunLineMarkerContributor extends RunLineMarkerContributor{
    protected static Icon ICON_RUN_SCENARIO = AllIcons.RunConfigurations.TestState.Run;
    protected static Icon ICON_RUN_PARAMS = AllIcons.RunConfigurations.TestState.Run_run;

    protected boolean isScenario(PyClass cls) {
        String clsName = String.valueOf(cls.getName());
        if (!clsName.startsWith("Scenario")) {
            return false;
        }
        return cls.isSubclass("vedro._scenario.Scenario", null);
    }

    protected boolean isParams(PyDecorator decorator) {
        String decoratorName = String.valueOf(decorator.getName());
        if (!decoratorName.equals("params")) {
            return false;
        }

        PyFunction target = decorator.getTarget();
        if (target == null) {
            return false;
        }
        String targetName = String.valueOf(target.getName());
        if (!targetName.equals("__init__")) {
            return false;
        }

        PyClass cls = target.getContainingClass();
        return cls != null && isScenario(cls);
    }

    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        if (element instanceof PyClass && isScenario((PyClass) element)) {
            return new Info(ICON_RUN_SCENARIO, ExecutorAction.getActions(), null);
        }
        if (element instanceof PyDecorator && isParams((PyDecorator) element)) {
            return new Info(ICON_RUN_PARAMS, ExecutorAction.getActions(), null);
        }
        return null;
    }
}
