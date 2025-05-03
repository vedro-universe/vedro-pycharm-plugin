package io.vedro;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyDecorator;
import com.jetbrains.python.psi.PyDecoratorList;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;


public class VedroConfigurationProducer extends LazyRunConfigurationProducer<VedroRunConfiguration> {
    @Override
    public @NotNull ConfigurationFactory getConfigurationFactory() {
        return VedroConfigurationFactory.getInstance();
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull VedroRunConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
        PsiElement element = sourceElement.get();
        return setupConfiguration(configuration, element);
    }

    protected boolean setupConfiguration(@NotNull VedroRunConfiguration configuration, @NotNull PsiElement element) {
        if (element instanceof PyClass) {
            return setupConfigurationForPyClass(configuration, (PyClass) element);
        }
        if (element instanceof PyDecorator) {
            return setupConfigurationForPyDecorator(configuration, (PyDecorator) element);
        }
        if (element instanceof PyFunction) {
            return setupConfigurationForPyFunction(configuration, (PyFunction) element);
        }
        return false;
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

        // Use the same format as class-based scenarios but with function name
        updateConfiguration(configuration, workingDir, target + "::Scenario_" + functionName);

        return true;
    }

    protected void updateConfiguration(@NotNull VedroRunConfiguration configuration, @NotNull Path workingDirectory, @NotNull String target) {
        configuration.setWorkingDirectory(workingDirectory.toString());
        configuration.setTarget(target);

        configuration.setRunnerOptions("-r rich pycharm --pycharm-no-output");

        configuration.setSuggestedName("vedro scenario");
        configuration.setActionName("vedro scenario");
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull VedroRunConfiguration configuration, @NotNull ConfigurationContext context) {
        Location<?> location = context.getLocation();
        if (location == null) {
            return false;
        }
        PsiElement element = location.getPsiElement();
        VedroRunConfiguration tmpConf = new VedroRunConfiguration(configuration.getProject(), configuration.getFactory());
        if (!setupConfiguration(tmpConf, element)) {
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
            String name = String.valueOf(d.getName());
            if (!name.equals("params")) {
                continue;
            }
            if (d.isEquivalentTo(decorator)) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
