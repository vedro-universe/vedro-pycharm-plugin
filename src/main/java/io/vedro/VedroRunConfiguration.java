package io.vedro;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.util.WriteExternalException;
import com.jetbrains.python.testing.AbstractPythonTestRunConfiguration;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VedroRunConfiguration extends AbstractPythonTestRunConfiguration<VedroRunConfiguration> {
    protected String target = "";
    protected String runnerOptions = "";

    protected String actionName = "";
    protected String suggestedName = "";

    protected String configFileName = "vedro.cfg.py";

    public VedroRunConfiguration(Project project, ConfigurationFactory factory) {
        super(project, factory);
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
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
        return new VedroSettingsEditorForm(getProject(), this);
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        return new VedroCommandLineState(this, environment);
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        JDOMExternalizerUtil.writeField(element, "RUNNER_OPTIONS", runnerOptions);
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        runnerOptions = JDOMExternalizerUtil.readField(element, "RUNNER_OPTIONS");
    }
}
