package io.vedro.ui.markers;

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

import io.vedro.util.VedroTestUtils;

public class VedroRunLineMarkerContributor extends RunLineMarkerContributor{
    protected static Icon ICON_RUN_SCENARIO = AllIcons.RunConfigurations.TestState.Run;
    protected static Icon ICON_RUN_PARAMS = AllIcons.RunConfigurations.TestState.Run_run;

    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        if (element.getFirstChild() == null) {
            PsiElement parent = element.getParent();

            if (parent instanceof PyClass cls
                && element.equals(cls.getNameIdentifier())
                && VedroTestUtils.isScenarioClass(cls)) {
                return new Info(ICON_RUN_SCENARIO, ExecutorAction.getActions(), null);
            }

            if (parent instanceof PyFunction fn
                && element.equals(fn.getNameIdentifier())
                && VedroTestUtils.isScenarioFunction(fn)) {
                return new Info(ICON_RUN_SCENARIO, ExecutorAction.getActions(), null);
            }

            return null;
        }
        if (element instanceof PyDecorator dec
            && VedroTestUtils.isParamsDecorator(dec)) {
            return new Info(ICON_RUN_PARAMS, ExecutorAction.getActions(), null);
        }
        if (element instanceof PyCallExpression call
            && VedroTestUtils.isParamsCall(call)) {
            return new Info(ICON_RUN_PARAMS, ExecutorAction.getActions(), null);
        }
        return null;
    }
}
