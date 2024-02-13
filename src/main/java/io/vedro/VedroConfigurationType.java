package io.vedro;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import icons.PythonIcons;
import org.jetbrains.annotations.NotNull;


public class VedroConfigurationType extends ConfigurationTypeBase {
    public static ConfigurationType INSTANCE = new VedroConfigurationType();

    public VedroConfigurationType() {
        super("vedro", "Vedro", "Vedro Plugin", PythonIcons.Python.PythonTests);
    }

    @NotNull
    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{VedroConfigurationFactory.getInstance()};
    }
}
