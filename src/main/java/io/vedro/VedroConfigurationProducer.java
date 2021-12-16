package io.vedro;

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

        if (element instanceof PyClass) {
            VirtualFile file = element.getContainingFile().getVirtualFile();
            Path bootstrapPath = findBootstrap(file, configuration.getProject().getBasePath(), configuration.getDefaultBoostrapName());
            if (bootstrapPath == null) {
                return false;
            }

            Path boostrapDir  = bootstrapPath.getParent();
            configuration.setWorkingDirectory(boostrapDir.toString());
            configuration.setBootstrapPath(bootstrapPath.toString());

            Path filePath = Paths.get(file.getPath());
            configuration.setTarget(boostrapDir.relativize(filePath).toString());
            configuration.setRunnerOptions("-r pycharm");

            configuration.setSuggestedName("vedro: run scenario");
            configuration.setActionName("vedro: run scenario");

            return true;
        }

        if (element instanceof PyDecorator) {
            String clsName = getClassName((PyDecorator) element);
            int decoratorIndex = getDecoratorIndex((PyDecorator) element);
            if (clsName == null || decoratorIndex == -1) {
                return false;
            }

            VirtualFile file = element.getContainingFile().getVirtualFile();
            Path bootstrapPath = findBootstrap(file, configuration.getProject().getBasePath(), configuration.getDefaultBoostrapName());
            if (bootstrapPath == null) {
                return false;
            }

            Path boostrapDir  = bootstrapPath.getParent();
            configuration.setWorkingDirectory(boostrapDir.toString());
            configuration.setBootstrapPath(bootstrapPath.toString());

            Path filePath = Paths.get(file.getPath());
            String target = boostrapDir.relativize(filePath).toString();
            configuration.setTarget(target + "::" + clsName + "#" + decoratorIndex);
            configuration.setRunnerOptions("-r pycharm");

            configuration.setSuggestedName("vedro: run scenario");
            configuration.setActionName("vedro: run scenario");

            return true;
        }

        return false;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull VedroRunConfiguration configuration, @NotNull ConfigurationContext context) {
        return false;
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
