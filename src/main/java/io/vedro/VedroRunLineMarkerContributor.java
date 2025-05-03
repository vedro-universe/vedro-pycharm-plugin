package io.vedro;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyDecorator;
import com.jetbrains.python.psi.PyDecoratorList;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyQualifiedNameOwner;
import com.jetbrains.python.psi.PyReferenceExpression;


public class VedroRunLineMarkerContributor extends RunLineMarkerContributor{
    protected static Icon ICON_RUN_SCENARIO = AllIcons.RunConfigurations.TestState.Run;
    protected static Icon ICON_RUN_PARAMS = AllIcons.RunConfigurations.TestState.Run_run;

    private static final String SCENARIO_BASE_CLASS = "vedro._scenario.Scenario";
    private static final String SCENARIO_FN_DECORATOR = "vedro_fn._scenario_decorator.scenario";
    private static final String PARAMS_DECORATOR = "vedro._params.params";

    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        if (element instanceof PyClass && isClsScenario((PyClass) element)) {
            return new Info(ICON_RUN_SCENARIO, ExecutorAction.getActions(), null);
        }
        if (element instanceof PyDecorator && isParams((PyDecorator) element)) {
            return new Info(ICON_RUN_PARAMS, ExecutorAction.getActions(), null);
        }
        if (element instanceof PyFunction && isFnScenario((PyFunction) element)) {
            return new Info(ICON_RUN_SCENARIO, ExecutorAction.getActions(), null);
        }
        if (element instanceof PyCallExpression && isFnParams((PyCallExpression) element)) {
            return new Info(ICON_RUN_PARAMS, ExecutorAction.getActions(), null);
        }
        return null;
    }

    protected boolean isClsScenario(PyClass cls) {
        return cls.isSubclass(SCENARIO_BASE_CLASS, null);
    }

    protected boolean isParams(PyDecorator decorator) {
        PyExpression callee = decorator.getCallee();
        if (!hasQualifiedName(callee, PARAMS_DECORATOR)) {
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

        PyClass containingClass = target.getContainingClass();
        return containingClass != null && isClsScenario(containingClass);
    }

    protected boolean isFnScenario(PyFunction function) {
        PyDecoratorList decoratorList = function.getDecoratorList();
        if (decoratorList == null) {
            return false;
        }

        for (PyDecorator decorator : decoratorList.getDecorators()) {
            PyExpression callee = decorator.getCallee();
            if (hasQualifiedName(callee, SCENARIO_FN_DECORATOR)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFnParams(@NotNull PyCallExpression call) {
        PyExpression callee = call.getCallee();
        if (!hasQualifiedName(callee, PARAMS_DECORATOR)) {
            return false;
        }

        PyDecorator decorator = PsiTreeUtil.getParentOfType(call, PyDecorator.class);
        if (decorator == null || !hasQualifiedName(decorator.getCallee(), SCENARIO_FN_DECORATOR)) {
            return false;
        }

        return true;
    }

    private static boolean hasQualifiedName(@NotNull PyExpression ref, @NotNull String qualifiedName) {
        if (!(ref instanceof PyReferenceExpression)) {
            return false;
        }
        PsiElement resolved = ((PyReferenceExpression) ref).getReference().resolve();
        if (!(resolved instanceof PyQualifiedNameOwner)) {
            return false;
        }
        String name = ((PyQualifiedNameOwner) resolved).getQualifiedName();
        return name != null && name.equals(qualifiedName);
    }
}
