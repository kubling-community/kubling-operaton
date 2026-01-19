package com.kubling.samples.operaton;

import com.kubling.samples.operaton.support.DataPlaneContainersConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.operaton.bpm.engine.HistoryService;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SystemStubsExtension.class)
public abstract class AbstractOperatonIntegrationTest {

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected HistoryService historyService;

    @BeforeAll
    static void setupEnvironment() {
        System.setProperty("KUBLING_PORT", String.valueOf(DataPlaneContainersConfig.getKublingPort()));
//        System.setProperty("KUBLING_PORT", "35482");
    }

    @BeforeEach
    void deployProcesses() {
        deployIfMissing(repositoryService, "minimal-process", "minimal-process");
        deployIfMissing(repositoryService, "minimal-process-with-wait", "minimal-process-with-wait");
    }

    public static void deployIfMissing(RepositoryService repo, String key, String name) {
        long count = repo.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .count();

        if (count == 0) {
            repo.createDeployment()
                    .name(name)
                    .addClasspathResource("processes/%s.bpmn".formatted(name))
                    .deploy();
        }

        verifyDeployment(repo, name);
    }

    public static void verifyDeployment(RepositoryService repo, String name) {
        long count = repo.createProcessDefinitionQuery()
                .processDefinitionName(name)
                .count();

        if (count == 0) {
            throw new IllegalStateException("Process definition '%s' not deployed.".formatted(name));
        }
    }

}
