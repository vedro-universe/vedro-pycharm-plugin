package io.vedro;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.util.ProgramParametersConfigurator;
import com.jetbrains.python.HelperPackage;
import com.jetbrains.python.run.CommandLinePatcher;
import com.jetbrains.python.run.PythonExecution;
import com.jetbrains.python.run.PythonModuleExecution;
import com.jetbrains.python.run.PythonScriptTargetedCommandLineBuilder;
import com.jetbrains.python.run.target.HelpersAwareTargetEnvironmentRequest;
import com.jetbrains.python.testing.PythonTestCommandLineStateBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VedroCommandLineState extends PythonTestCommandLineStateBase<VedroRunConfiguration> {
    public VedroCommandLineState(VedroRunConfiguration configuration, ExecutionEnvironment environment) {
        super(configuration, environment);
    }

    @Override
    public @Nullable ExecutionResult execute(@NotNull Executor executor, @NotNull PythonScriptTargetedCommandLineBuilder converter) throws ExecutionException {
        ProcessHandler processHandler = startProcess(converter);
        ConsoleView console = invokeAndWait(() -> createAndAttachConsole(myConfiguration.getProject(), processHandler, executor));

        return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler));
    }

    @Override
    protected @NotNull PythonExecution buildPythonExecution(@NotNull HelpersAwareTargetEnvironmentRequest helpersAwareRequest) {
        PythonModuleExecution moduleExecution = new PythonModuleExecution();
        moduleExecution.setModuleName("vedro");
        moduleExecution.addParameter("run");

        moduleExecution.addParameter(myConfiguration.getTarget());

        List<String> parameters = ProgramParametersConfigurator.expandMacrosAndParseParameters(myConfiguration.getRunnerOptions());
        moduleExecution.addParameters(parameters);
        return moduleExecution;
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

    @Override
    protected @NotNull List<String> getTestSpecs() {
        return new ArrayList<>();
    }
}
