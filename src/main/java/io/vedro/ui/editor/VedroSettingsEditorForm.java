package io.vedro.ui.editor;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.run.AbstractPyCommonOptionsForm;
import com.jetbrains.python.run.PyCommonOptionsFormData;
import com.jetbrains.python.run.PyCommonOptionsFormFactory;

import io.vedro.config.VedroRunConfiguration;

public class VedroSettingsEditorForm extends SettingsEditor<VedroRunConfiguration> {
    protected JPanel rootPanel;
    protected JTextField runnerOptionsField;

    protected AbstractPyCommonOptionsForm commonOptionsForm;
    protected JPanel commonOptionsPlaceholder;

    public VedroSettingsEditorForm(Project project, VedroRunConfiguration configuration) {
        PyCommonOptionsFormData formData = configuration.getCommonOptionsFormData();
        commonOptionsForm = PyCommonOptionsFormFactory.getInstance().createForm(formData);
        commonOptionsPlaceholder.add(commonOptionsForm.getMainPanel());
    }

    @Override
    protected void resetEditorFrom(@NotNull VedroRunConfiguration configuration) {
        VedroRunConfiguration.copyParams(configuration, commonOptionsForm);
        runnerOptionsField.setText(configuration.getRunnerOptions());
    }

    @Override
    protected void applyEditorTo(@NotNull VedroRunConfiguration configuration) throws ConfigurationException {
        VedroRunConfiguration.copyParams(commonOptionsForm, configuration);
        configuration.setRunnerOptions(runnerOptionsField.getText());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return rootPanel;
    }
}
