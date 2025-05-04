package io.vedro;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyDecorator;
import com.jetbrains.python.psi.PyFunction;


public class VedroRunLineMarkerContributor extends RunLineMarkerContributor{
    protected static Icon ICON_RUN_SCENARIO = AllIcons.RunConfigurations.TestState.Run;
    protected static Icon ICON_RUN_PARAMS = AllIcons.RunConfigurations.TestState.Run_run;

    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        if (element instanceof PyClass && VedroTestUtils.isScenarioClass((PyClass) element)) {
            return new Info(ICON_RUN_SCENARIO, ExecutorAction.getActions(), null);
        }
        if (element instanceof PyDecorator && VedroTestUtils.isParamsDecorator((PyDecorator) element)) {
            return new Info(ICON_RUN_PARAMS, ExecutorAction.getActions(), null);
        }
        if (element instanceof PyFunction && VedroTestUtils.isScenarioFunction((PyFunction) element)) {
            return new Info(ICON_RUN_SCENARIO, ExecutorAction.getActions(), null);
        }
        if (element instanceof PyCallExpression && VedroTestUtils.isParamsCall((PyCallExpression) element)) {
            return new Info(ICON_RUN_PARAMS, ExecutorAction.getActions(), null);
        }
        return null;
    }
}
