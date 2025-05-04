package io.vedro;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyDecorator;
import com.jetbrains.python.psi.PyDecoratorList;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyQualifiedNameOwner;
import com.jetbrains.python.psi.PyReferenceExpression;

public class VedroConfigurationProducer extends LazyRunConfigurationProducer<VedroRunConfiguration> {
    private static final String SCENARIO_FN_DECORATOR = "vedro_fn._scenario_decorator.scenario";
    private static final String PARAMS_DECORATOR = "vedro._params.params";

    @Override
    public @NotNull ConfigurationFactory getConfigurationFactory() {
        return VedroConfigurationFactory.getInstance();
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull VedroRunConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
        PsiElement psi = context.getPsiLocation();
        if (psi == null) {
            return false;
        }

        if (!setupConfiguration(configuration, psi)) {
            return false;
        }

        sourceElement.set(psi);
        return true;
    }

    protected boolean setupConfiguration(@NotNull VedroRunConfiguration configuration, @NotNull PsiElement element) {
        if (element instanceof PyFile) {
            return setupConfigurationForPyFile(configuration, (PyFile) element);
        }
        if (element instanceof PsiDirectory) {
            return setupConfigurationForDirectory(configuration, (PsiDirectory) element);
        }

        if (element instanceof PyClass) {
            return setupConfigurationForPyClass(configuration, (PyClass) element);
        }
        if (element instanceof PyDecorator) {
            return setupConfigurationForPyDecorator(configuration, (PyDecorator) element);
        }

        if (element instanceof PyFunction) {
            return setupConfigurationForPyFunction(configuration, (PyFunction) element);
        }
        if (element instanceof PyCallExpression) {
            return setupConfigurationForParamsCall(configuration, (PyCallExpression) element);
        }
        return false;
    }

    protected boolean setupConfigurationForDirectory(@NotNull VedroRunConfiguration configuration, @NotNull PsiDirectory element) {
        VirtualFile dir = element.getVirtualFile();
        if (dir == null) {
            return false;
        }
        
        Path dirPath = Paths.get(dir.getPath());

        Path configFile = findConfigFile(dir, configuration.getProject().getBasePath(), configuration.getConfigFileName());
        Path workingDir = (configFile != null) ? configFile.getParent() : Paths.get(configuration.getWorkingDirectorySafe());
        String target = workingDir.relativize(dirPath).toString();

        updateConfiguration(configuration, workingDir, target);

        // configuration.setActionName("Vedro scenarios in " + dir.getName() + "/");

        return true;
    }

    protected boolean setupConfigurationForPyFile(@NotNull VedroRunConfiguration configuration, @NotNull PyFile element) {
        VirtualFile file = element.getVirtualFile();
        if (file == null) {
            return false;
        }

        Path filePath = Paths.get(file.getPath());

        Path configFile = findConfigFile(file, configuration.getProject().getBasePath(), configuration.getConfigFileName());        
        Path workingDir = (configFile != null) ? configFile.getParent() : Paths.get(configuration.getWorkingDirectorySafe());
        String target = workingDir.relativize(filePath).toString();

        updateConfiguration(configuration, workingDir, target);

        // configuration.setActionName("Vedro scenarios in " + file.getName());

        return true;
    }

    protected boolean setupConfigurationForPyClass(@NotNull VedroRunConfiguration configuration, @NotNull PyClass element) {
        String clsName = element.getName();
        if (clsName == null) {
            return false;
        }
        VirtualFile file = element.getContainingFile().getVirtualFile();
        Path filePath = Paths.get(file.getPath());

        Path configFile = findConfigFile(file, configuration.getProject().getBasePath(), configuration.getConfigFileName());        
        Path workingDir = (configFile != null) ? configFile.getParent() : Paths.get(configuration.getWorkingDirectorySafe());
        String target = workingDir.relativize(filePath).toString();

        updateConfiguration(configuration, workingDir, target + "::" + clsName);

        return true;
    }

    protected boolean setupConfigurationForPyDecorator(@NotNull VedroRunConfiguration configuration, @NotNull PyDecorator element) {
        String clsName = getClassName(element);
        int decoratorIndex = getDecoratorIndex(element);
        if (clsName == null || decoratorIndex == -1) {
            return false;
        }

        VirtualFile file = element.getContainingFile().getVirtualFile();
        Path filePath = Paths.get(file.getPath());

        Path configFile = findConfigFile(file, configuration.getProject().getBasePath(), configuration.getConfigFileName());
        Path workingDir = (configFile != null) ? configFile.getParent() : Paths.get(configuration.getWorkingDirectorySafe());
        String target = workingDir.relativize(filePath).toString();

        updateConfiguration(configuration, workingDir, target + "::" + clsName + "#" + decoratorIndex);

        return true;
    }

    protected boolean setupConfigurationForPyFunction(@NotNull VedroRunConfiguration configuration, @NotNull PyFunction element) {
        String functionName = element.getName();
        if (functionName == null) {
            return false;
        }

        VirtualFile file = element.getContainingFile().getVirtualFile();
        Path filePath = Paths.get(file.getPath());

        Path configFile = findConfigFile(file, configuration.getProject().getBasePath(), configuration.getConfigFileName());
        Path workingDir = (configFile != null) ? configFile.getParent() : Paths.get(configuration.getWorkingDirectorySafe());
        String target = workingDir.relativize(filePath).toString();

        updateConfiguration(configuration, workingDir, target + "::Scenario_" + functionName);

        return true;
    }

    protected boolean setupConfigurationForParamsCall(@NotNull VedroRunConfiguration configuration, @NotNull PyCallExpression call) {
        PyFunction function = PsiTreeUtil.getParentOfType(call, PyFunction.class);
        if (function == null) {
            return false;
        }

        int callIndex = getParamsCallIndex(call);
        if (callIndex == -1) {
            return false;
        }

        VirtualFile file = function.getContainingFile().getVirtualFile();
        Path filePath = Paths.get(file.getPath());

        Path configFile = findConfigFile(file, configuration.getProject().getBasePath(), configuration.getConfigFileName());
        Path workingDir = (configFile != null) ? configFile.getParent() : Paths.get(configuration.getWorkingDirectorySafe());
        String target = workingDir.relativize(filePath).toString();

        updateConfiguration(configuration, workingDir, target + "::Scenario_" + function.getName() + "#" + callIndex);

        return true;
    }

    protected void updateConfiguration(@NotNull VedroRunConfiguration configuration, @NotNull Path workingDirectory, @NotNull String target) {
        configuration.setWorkingDirectory(workingDirectory.toString());
        configuration.setTarget(target);

        configuration.setRunnerOptions("-r rich pycharm --pycharm-no-output");

        configuration.setSuggestedName(getSuggestedName(configuration.getProject(), workingDirectory));
        configuration.setActionName("Vedro scenario(s)");
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull VedroRunConfiguration configuration, @NotNull ConfigurationContext context) {
        PsiElement psi = context.getPsiLocation();
        if (psi == null) {
            return false;
        }

        VedroRunConfiguration tmpConf = new VedroRunConfiguration(configuration.getProject(), configuration.getFactory());
        if (!setupConfiguration(tmpConf, psi)) {
            return false;
        }

        if (!configuration.getWorkingDirectory().equals(tmpConf.getWorkingDirectory())) {
            return false;
        }

        // Move to findExistingConfiguration
        configuration.setTarget(tmpConf.getTarget());
        return true;
    }

    @Nullable
    protected Path findConfigFile(VirtualFile file, String projectPath, String configFileName) {
        if (file.isDirectory()) {
            VirtualFile[] children = file.getChildren();
            for (VirtualFile child : children) {
                if (child.getName().equals(configFileName)) {
                    return Paths.get(child.getPath());
                }
            }
        }
        VirtualFile parent = file.getParent();
        if (parent == null || file.getPath().equals(projectPath)) {
            return null;
        }
        return findConfigFile(parent, projectPath, configFileName);
    }

    @Nullable
    protected String getClassName(@NotNull PyDecorator decorator) {
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

    protected int getDecoratorIndex(@NotNull PyDecorator decorator) {
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

    protected int getParamsCallIndex(@NotNull PyCallExpression call) {
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
            Collection<PyCallExpression> allCalls = PsiTreeUtil.findChildrenOfType(decorator, PyCallExpression.class);
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

    protected static @NotNull String getSuggestedName(@NotNull Project project, @NotNull Path workingDir) {
        Path projectRoot = Paths.get(project.getBasePath());
        String rel = projectRoot.relativize(workingDir).toString();
        return "Vedro scenarios in '" + (rel.isEmpty() ? "./" : rel) + "'";
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
