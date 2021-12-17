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
        return VedroConfigurationFactory.INSTANCE;
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
        return false;
    }

    protected boolean setupConfigurationForPyClass(@NotNull VedroRunConfiguration configuration, @NotNull PyClass element) {
        VirtualFile file = element.getContainingFile().getVirtualFile();
        Path bootstrapPath = findBootstrap(file, configuration.getProject().getBasePath(), configuration.getDefaultBoostrapName());
        if (bootstrapPath == null) {
            Path workingDirectory = Paths.get(configuration.getWorkingDirectory());
            Path defaultBootstrapPath = Paths.get(configuration.getDefaultBoostrapName());
            updateConfiguration(configuration, workingDirectory, defaultBootstrapPath, file.getPath());
            return true;
        }

        Path boostrapDir  = bootstrapPath.getParent();
        Path filePath = Paths.get(file.getPath());
        String target = boostrapDir.relativize(filePath).toString();

        updateConfiguration(configuration, boostrapDir, bootstrapPath, target);

        return true;
    }

    protected boolean setupConfigurationForPyDecorator(@NotNull VedroRunConfiguration configuration, @NotNull PyDecorator element) {
        String clsName = getClassName(element);
        int decoratorIndex = getDecoratorIndex(element);
        if (clsName == null || decoratorIndex == -1) {
            return false;
        }

        VirtualFile file = element.getContainingFile().getVirtualFile();
        Path bootstrapPath = findBootstrap(file, configuration.getProject().getBasePath(), configuration.getDefaultBoostrapName());
        if (bootstrapPath == null) {
            Path workingDirectory = Paths.get(configuration.getWorkingDirectory());
            Path defaultBootstrapPath = Paths.get(configuration.getDefaultBoostrapName());
            updateConfiguration(configuration, workingDirectory, defaultBootstrapPath, file.getPath());
            return true;
        }

        Path boostrapDir  = bootstrapPath.getParent();
        Path filePath = Paths.get(file.getPath());
        String target = boostrapDir.relativize(filePath).toString();

        updateConfiguration(configuration, boostrapDir, bootstrapPath, target + "::" + clsName + "#" + decoratorIndex);

        return true;
    }

    protected void updateConfiguration(@NotNull VedroRunConfiguration configuration, @NotNull Path workingDirectory, @NotNull Path bootstrapPath, @NotNull String target) {
        configuration.setWorkingDirectory(workingDirectory.toString());
        configuration.setBootstrapPath(bootstrapPath.toString());

        configuration.setTarget(target);
        configuration.setRunnerOptions("-r pycharm");

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

        if (!configuration.getBootstrapPath().equals(tmpConf.getBootstrapPath())) {
            return false;
        }
        // Move to findExistingConfiguration
        configuration.setTarget(tmpConf.getTarget());
        return true;
    }

    @Nullable
    protected Path findBootstrap(VirtualFile file, String projectPath, String boostrapName) {
        if (file.isDirectory()) {
            VirtualFile[] children = file.getChildren();
            for (VirtualFile child : children) {
                if (child.getName().equals(boostrapName)) {
                    return Paths.get(child.getPath());
                }
            }
        }
        VirtualFile parent = file.getParent();
        if (parent == null || file.getPath().equals(projectPath)) {
            return null;
        }
        return findBootstrap(parent, projectPath, boostrapName);
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
