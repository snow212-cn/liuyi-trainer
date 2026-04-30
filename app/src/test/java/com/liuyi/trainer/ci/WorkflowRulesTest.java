package com.liuyi.trainer.ci;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Test;

public class WorkflowRulesTest {
    private static final String WORKFLOW_PATH = ".github/workflows/android-debug-apk.yml";

    @Test
    public void workflowIncludesDevelop1PushTrigger() throws IOException {
        String workflow = readWorkflow();
        assertTrue(workflow.contains("- develop1"));
    }

    @Test
    public void workflowPublishesReleaseFromDevelop1() throws IOException {
        String workflow = readWorkflow();
        assertTrue(workflow.contains("github.ref == 'refs/heads/develop1'"));
    }

    @Test
    public void workflowForcesDebugModeForDevelop1() throws IOException {
        String workflow = readWorkflow();
        assertTrue(workflow.contains("if [[ \"${GITHUB_REF}\" == refs/heads/develop1 ]]; then"));
        assertTrue(workflow.contains("requested_mode=\"debug\""));
    }

    private static String readWorkflow() throws IOException {
        File[] candidates = new File[] {
                new File(WORKFLOW_PATH),
                new File("../" + WORKFLOW_PATH)
        };
        for (File candidate : candidates) {
            if (candidate.exists()) {
                return new String(Files.readAllBytes(candidate.toPath()), StandardCharsets.UTF_8);
            }
        }
        throw new IOException("Workflow file not found from test working directory");
    }
}
