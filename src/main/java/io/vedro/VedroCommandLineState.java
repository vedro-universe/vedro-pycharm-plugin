package io.vedro;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.jetbrains.python.HelperPackage;
import com.jetbrains.python.run.CommandLinePatcher;
import com.jetbrains.python.testing.PythonTestCommandLineStateBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class VedroCommandLineState extends PythonTestCommandLineStateBase<VedroRunConfiguration> {
    public VedroCommandLineState(VedroRunConfiguration configuration, ExecutionEnvironment environment) {
        super(configuration, environment);
    }

    @Override
    protected HelperPackage getRunner() {
        return null;
    }

    @Override
    public ExecutionResult execute(Executor executor, PythonProcessStarter processStarter, CommandLinePatcher... patchers) throws ExecutionException {
        final ProcessHandler processHandler = startProcess(processStarter, patchers);
        ConsoleView console = invokeAndWait(() -> createAndAttachConsole(myConfiguration.getProject(), processHandler, executor));

        return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler));
    }

    protected void addTestRunnerParameters(GeneralCommandLine cmd) {
        ParamsGroup scriptParams = cmd.getParametersList().getParamsGroup(GROUP_SCRIPT);
        assert scriptParams != null;

        scriptParams.addParameter(myConfiguration.getBootstrapPath());
        scriptParams.addParameter(myConfiguration.getTarget());

        addBeforeParameters(cmd);
        scriptParams.addParametersString(myConfiguration.getRunnerOptions());
        addAfterParameters(cmd);
    }

    @Override
    protected @NotNull List<String> getTestSpecs() {
        List<String> testSpecs = new ArrayList<>();
        return testSpecs;
    }
}
