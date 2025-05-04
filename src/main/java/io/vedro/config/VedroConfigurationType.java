package io.vedro.config;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeBase;

import io.vedro.ui.icons.PluginIcons;

public class VedroConfigurationType extends ConfigurationTypeBase {
    public static ConfigurationType INSTANCE = new VedroConfigurationType();

    public VedroConfigurationType() {
        super("vedro", "Vedro", "Vedro Plugin", PluginIcons.PYTHON);
    }

    @NotNull
    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{VedroConfigurationFactory.getInstance()};
    }
}
