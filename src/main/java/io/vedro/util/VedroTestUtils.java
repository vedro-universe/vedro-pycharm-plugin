package io.vedro.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

public class VedroTestUtils {
    public static final String SCENARIO_BASE_CLASS = "vedro._scenario.Scenario";
    public static final String SCENARIO_FN_DECORATOR = "vedro_fn._scenario_decorator.scenario";
    public static final String PARAMS_DECORATOR = "vedro._params.params";

    /**
     * Checks if a PyClass is a Vedro scenario class
     */
    public static boolean isScenarioClass(@NotNull PyClass cls) {
        return cls.isSubclass(SCENARIO_BASE_CLASS, null);
    }

    /**
     * Checks if a PyDecorator is a Vedro params decorator for a scenario class
     */
    public static boolean isParamsDecorator(@NotNull PyDecorator decorator) {
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
        return containingClass != null && isScenarioClass(containingClass);
    }

    /**
     * Checks if a PyFunction is a Vedro function-style scenario
     */
    public static boolean isScenarioFunction(@NotNull PyFunction function) {
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

    /**
     * Checks if a PyCallExpression is a Vedro params call in a function-style scenario
     */
    public static boolean isParamsCall(@NotNull PyCallExpression call) {
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

    /**
     * Gets the class name from a decorator
     */
    @Nullable
    public static String getClassName(@NotNull PyDecorator decorator) {
        PyFunction function = decorator.getTarget();
        if (function == null) {
            return null;
        }
        PyClass cls = function.getContainingClass();
        if (cls == null) {
            return null;
        }
        return cls.getName();
    }

    /**
     * Gets the index of a params decorator 
     */
    public static int getParamsDecoratorIndex(@NotNull PyDecorator decorator) {
        PyFunction function = decorator.getTarget();
        if (function == null) {
            return -1;
        }
        PyDecoratorList decorators = function.getDecoratorList();
        if (decorators == null) {
            return -1;
        }
        int index = 1;
        for (PyDecorator d : decorators.getDecorators()) {
            if (hasQualifiedName(d.getCallee(), PARAMS_DECORATOR)) {
                if (d.isEquivalentTo(decorator)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

    /**
     * Gets the index of a params call in a function-style scenario
     */
    public static int getParamsCallIndex(@NotNull PyCallExpression call) {
        PyFunction function = PsiTreeUtil.getParentOfType(call, PyFunction.class);
        if (function == null) {
            return -1;
        }

        PyDecoratorList decoratorList = function.getDecoratorList();
        if (decoratorList == null) {
            return -1;
        }

        for (PyDecorator decorator : decoratorList.getDecorators()) {
            if (!hasQualifiedName(decorator.getCallee(), SCENARIO_FN_DECORATOR)) {
                continue;
            }

            int index = 1;
            java.util.Collection<PyCallExpression> allCalls = PsiTreeUtil.findChildrenOfType(decorator, PyCallExpression.class);
            for (PyCallExpression currentCall : allCalls) {
                if (hasQualifiedName(currentCall.getCallee(), PARAMS_DECORATOR)) {
                    if (currentCall.isEquivalentTo(call)) {
                        return index;
                    }
                    index++;
                }
            }
        }

        return -1;
    }

    public static boolean hasQualifiedName(@NotNull PyExpression ref, @NotNull String qualifiedName) {
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
