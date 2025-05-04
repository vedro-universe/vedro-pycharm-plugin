package io.vedro.ui.markers;

import java.util.function.Function;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.PyTargetExpression;

import io.vedro.util.VedroTestUtils;

public class VedroSubjectLineMarkerContributor extends RunLineMarkerContributor {
    private static final Icon ICON_RENAME_FILE = AllIcons.Actions.Edit;

    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        if (element.getFirstChild() != null) {
            return null;
        }

        PsiElement parent = element.getParent();
        if (!(parent instanceof PyTargetExpression target) ||
            !element.equals(target.getNameIdentifier()) ||
            !"subject".equals(target.getName())) {
            return null;
        }

        PyAssignmentStatement assignment = PsiTreeUtil.getParentOfType(target, PyAssignmentStatement.class);
        if (assignment == null) {
            return null;
        }

        SubjectInfo subjectInfo = extractSubjectInfo(assignment);
        if (subjectInfo == null) {
            return null;
        }

        String curFilename = subjectInfo.containingFile.getName();
        String newFilename = convertSubjectToFilename(subjectInfo.subjectValue);
        if (curFilename.equals(newFilename)) {
            return null;
        }

        return createLineMarkerInfo(subjectInfo);
    }

    private Info createLineMarkerInfo(SubjectInfo subjectInfo) {
        AnAction renameFileAction = createRenameAction(subjectInfo);
        Function<? super PsiElement, String> tooltipProvider = psi -> "Rename file according to subject";

        return new Info(ICON_RENAME_FILE, new AnAction[]{renameFileAction}, tooltipProvider);
    }

    private AnAction createRenameAction(SubjectInfo subjectInfo) {
        return new AnAction(
            "Rename file to match subject",
            "Rename the Python file to match the scenario’s subject string",
            ICON_RENAME_FILE
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                Project project = e.getProject();
                if (project != null) {
                    renameFileFromSubject(project, subjectInfo);
                }
            }
        };
    }

    @Nullable
    private SubjectInfo extractSubjectInfo(PyAssignmentStatement assignment) {
        PyClass containingClass = PsiTreeUtil.getParentOfType(assignment, PyClass.class);
        if (containingClass == null || !VedroTestUtils.isScenarioClass(containingClass)) {
            return null;
        }

        PyExpression value = assignment.getAssignedValue();
        if (!(value instanceof PyStringLiteralExpression)) {
            return null;
        }

        String subjectValue = ((PyStringLiteralExpression) value).getStringValue().trim();
        if (subjectValue.isEmpty()) {
            return null;
        }
        VirtualFile containingFile = containingClass.getContainingFile().getVirtualFile();

        return new SubjectInfo(subjectValue, containingFile);
    }

    private void renameFileFromSubject(Project project, SubjectInfo subjectInfo) {
        String newFilename = convertSubjectToFilename(subjectInfo.subjectValue);

        // Skip if the filename is already correct
        if (subjectInfo.containingFile.getName().equals(newFilename)) {
            return;
        }

        // Rename the file in a write action
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    subjectInfo.containingFile.rename(this, newFilename);
                } catch (Exception e) {
                    String errorMsg = "Failed to rename file to '" + newFilename + "'";
                    Notifications.Bus.notify(
                        new Notification(
                            "Vedro.RenameFile", 
                            errorMsg + ": " + e.getMessage(),
                            NotificationType.ERROR
                        )
                    );
                }
            });
        });
    }

    private String convertSubjectToFilename(String subject) {
        String filtered = subject
            .toLowerCase()
            .replaceAll("\\s+", "_")
            .replaceAll("[^\\p{L}\\p{N}_\\-\\(\\)]", "");
        return filtered.isEmpty() ? "untitled.py" : (filtered + ".py");
    }

    private static class SubjectInfo {
        final String subjectValue;
        final VirtualFile containingFile;
        
        SubjectInfo(String subjectValue, VirtualFile containingFile) {
            this.subjectValue = subjectValue;
            this.containingFile = containingFile;
        }
    }
}
