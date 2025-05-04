package io.vedro.config;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.run.PythonConfigurationFactoryBase;

public class VedroConfigurationFactory extends PythonConfigurationFactoryBase {
    public static ConfigurationFactory INSTANCE;

    public static ConfigurationFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new VedroConfigurationFactory(VedroConfigurationType.INSTANCE);
        }
        return INSTANCE;
    }

    public VedroConfigurationFactory(@NotNull ConfigurationType configurationType) {
        super(configurationType);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new VedroRunConfiguration(project, this);
    }

    @Override
    public @NotNull
    @NonNls String getId() {
        return getType().getId();
    }
}
