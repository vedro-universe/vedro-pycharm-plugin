package io.vedro;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.jetbrains.python.run.AbstractPyCommonOptionsForm;
import com.jetbrains.python.run.PyCommonOptionsFormData;
import com.jetbrains.python.run.PyCommonOptionsFormFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class VedroSettingsEditorForm extends SettingsEditor<VedroRunConfiguration> {
    protected JPanel rootPanel;
    protected TextFieldWithBrowseButton bootstrapPathField;
    protected JTextField runnerOptionsField;

    protected AbstractPyCommonOptionsForm commonOptionsForm;
    protected JPanel commonOptionsPlaceholder;

    public VedroSettingsEditorForm(Project project, VedroRunConfiguration configuration) {
        PyCommonOptionsFormData formData = configuration.getCommonOptionsFormData();
        commonOptionsForm = PyCommonOptionsFormFactory.getInstance().createForm(formData);
        commonOptionsPlaceholder.add(commonOptionsForm.getMainPanel());

        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("py");
        bootstrapPathField.addBrowseFolderListener("Bootstrap Path", "Select bootstrap file", project, descriptor);
    }

    @Override
    protected void resetEditorFrom(@NotNull VedroRunConfiguration configuration) {
        VedroRunConfiguration.copyParams(configuration, commonOptionsForm);
        bootstrapPathField.setText(configuration.getBootstrapPath());
        runnerOptionsField.setText(configuration.getRunnerOptions());
    }

    @Override
    protected void applyEditorTo(@NotNull VedroRunConfiguration configuration) throws ConfigurationException {
        VedroRunConfiguration.copyParams(commonOptionsForm, configuration);
        configuration.setBootstrapPath(bootstrapPathField.getText());
        configuration.setRunnerOptions(runnerOptionsField.getText());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return rootPanel;
    }
}
