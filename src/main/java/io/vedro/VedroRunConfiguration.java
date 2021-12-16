package io.vedro;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.jetbrains.python.testing.AbstractPythonTestRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VedroRunConfiguration extends AbstractPythonTestRunConfiguration<VedroRunConfiguration> {
    protected String target = "";
    protected String bootstrapPath = "";
    protected String runnerOptions = "";

    protected String actionName = "";
    protected String suggestedName = "";

    protected String defaultBoostrapName = "bootstrap.py";

    public VedroRunConfiguration(Project project, ConfigurationFactory factory) {
        super(project, factory);
    }

    public String getDefaultBoostrapName() {
        return defaultBoostrapName;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public void setBootstrapPath(String bootstrapPath) {
        this.bootstrapPath = bootstrapPath;
    }

    public String getBootstrapPath() {
        return bootstrapPath;
    }

    public void setRunnerOptions(String runnerOptions) {
        this.runnerOptions = runnerOptions;
    }

    public String getRunnerOptions() {
        return runnerOptions;
    }

    @Override
    public @Nullable
    @NlsActions.ActionText String suggestedName() {
        return suggestedName;
    }

    public void setSuggestedName(String suggestedName) {
        this.suggestedName = suggestedName;
        setGeneratedName();
    }

    @Override
    public @Nullable
    @NlsActions.ActionText String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    @Override
    protected SettingsEditor<VedroRunConfiguration> createConfigurationEditor() {
        return null;
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        return new VedroCommandLineState(this, environment);
    }
}
