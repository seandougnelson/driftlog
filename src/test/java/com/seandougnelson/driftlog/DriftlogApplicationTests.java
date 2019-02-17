package com.seandougnelson.driftlog;

import com.seandougnelson.driftlog.api.IDriftlog;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import org.apache.commons.codec.Charsets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(secure = false)
public class DriftlogApplicationTests {

  private static final String TEST_DOCKER_IMAGE = "driftlog-test";
  private static final String TEST_DOCKER_CONTAINER = "driftlog_test";
  private static final String TEST_TMP_DIR = "test-tmp";
  private static final String PYTHON_DOCKER_IMAGE = "python:3.7-alpine";
  private static DockerClient docker;

  @TestConfiguration
  static class DriftlogApplicationTestContextConfiguration {

    @Bean
    public IDriftlog driftlog() {
      return new DriftlogImpl();
    }
  }

  @Autowired
  private MockMvc mockMvc;

  @BeforeClass
  public static void setUp() throws Exception {
    System.setProperty(Env.ALLOWED_LOG_DIRS.name(), TEST_TMP_DIR);
    System.setProperty(Env.ALLOWED_LOG_EXTENSIONS.name(), "log,.txt");

    docker = new DefaultDockerClient("unix:///var/run/docker.sock");
    DriftlogApplication.testDockerConnection(docker);

    cleanUp();

    // Create test docker images and containers
    docker.build(Paths.get("docker/" + TEST_DOCKER_IMAGE), TEST_DOCKER_IMAGE);

    docker.createContainer(ContainerConfig.builder().image(TEST_DOCKER_IMAGE).build(), TEST_DOCKER_CONTAINER);
    docker.startContainer(TEST_DOCKER_CONTAINER);

    Map<String, String> container2Labels = new HashMap<>();
    container2Labels.put("someLabelKey", "someLabelValue");
    docker.createContainer(ContainerConfig.builder().image(TEST_DOCKER_IMAGE).labels(container2Labels).build(),
            TEST_DOCKER_CONTAINER + "2");
    docker.startContainer(TEST_DOCKER_CONTAINER + "2");

    Map<String, String> container3Labels = new HashMap<>();
    container3Labels.put("someLabelKey", "someLabelValue");
    container3Labels.put("anotherLabelKey", "anotherLabelValue");
    docker.createContainer(ContainerConfig.builder().image(TEST_DOCKER_IMAGE).labels(container3Labels).build(),
            TEST_DOCKER_CONTAINER + "3");
    docker.startContainer(TEST_DOCKER_CONTAINER + "3");

    docker.createContainer(ContainerConfig.builder().image(TEST_DOCKER_IMAGE).build(), TEST_DOCKER_CONTAINER + "4");

    // Create test log files and directories
    Files.createDirectories(Paths.get(TEST_TMP_DIR + "/subdir-1/subdir-2/subdir-3"));

    List<String> testLog = Arrays.asList("Line 1", "Line 2", "Line 3");
    Files.write(Paths.get(TEST_TMP_DIR + "/test.log"), testLog, Charsets.UTF_8);

    List<String> testLogNoDotExtension = Arrays.asList("Alfa", "Bravo", "Charlie");
    Files.write(Paths.get(TEST_TMP_DIR + "/testlog"), testLogNoDotExtension, Charsets.UTF_8);

    List<String> testTxt = Arrays.asList("One", "Two", "Three");
    Files.write(Paths.get(TEST_TMP_DIR + "/subdir-1/subdir-2/test.txt"), testTxt, Charsets.UTF_8);

    Files.createFile(Paths.get(TEST_TMP_DIR + "/subdir-1/subdir-2/testtxt"));
    Files.createFile(Paths.get(TEST_TMP_DIR + "/subdir-1/subdir-2/subdir-3/random"));
  }

  @AfterClass
  public static void tearDown() {
    cleanUp();
    docker.close();
  }

  private static void cleanUp() {
    // Remove test docker containers and images
    try {
      docker.stopContainer(TEST_DOCKER_CONTAINER, 0);
      docker.removeContainer(TEST_DOCKER_CONTAINER, DockerClient.RemoveContainerParam.removeVolumes());
    } catch (Exception e) { /* ignored */ }

    try {
      docker.stopContainer(TEST_DOCKER_CONTAINER + "2", 0);
      docker.removeContainer(TEST_DOCKER_CONTAINER + "2", DockerClient.RemoveContainerParam.removeVolumes());
    } catch (Exception e) { /* ignored */ }

    try {
      docker.stopContainer(TEST_DOCKER_CONTAINER + "3", 0);
      docker.removeContainer(TEST_DOCKER_CONTAINER + "3", DockerClient.RemoveContainerParam.removeVolumes());
    } catch (Exception e) { /* ignored */ }

    try {
      docker.removeContainer(TEST_DOCKER_CONTAINER + "4", DockerClient.RemoveContainerParam.removeVolumes());
    } catch (Exception e) { /* ignored */ }

    try {
      docker.removeImage(TEST_DOCKER_IMAGE);
    } catch (Exception e) { /* ignored */ }

    try {
      docker.removeImage(PYTHON_DOCKER_IMAGE);
    } catch (Exception e) { /* ignored */ }

    // Remove test log files and directories
    try {
      Files.walk(Paths.get(TEST_TMP_DIR))
              .sorted(Comparator.reverseOrder())
              .map(Path::toFile)
              .forEach(File::delete);
    } catch (Exception e) { /* ignored */ }
  }

  @Test
  public void pathNotFound() throws Exception {
    this.mockMvc.perform(get("/"))
            .andExpect(status().isNotFound());
  }

  @Test
  public void log_startingAtFirstLine() throws Exception {
    String logPath = TEST_TMP_DIR + "/test.log";
    this.mockMvc.perform(get("/log?path=" + logPath))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(logPath))
            .andExpect(jsonPath("$.contentLineCount").value("3"))
            .andExpect(jsonPath("$.content[0]").value("Line 1"))
            .andExpect(jsonPath("$.content[1]").value("Line 2"))
            .andExpect(jsonPath("$.content[2]").value("Line 3"));
  }

  @Test
  public void log_startingAtLastLine() throws Exception {
    String logPath = TEST_TMP_DIR + "/test.log";
    this.mockMvc.perform(get("/log?path=" + logPath + "&startAtLine=2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(logPath))
            .andExpect(jsonPath("$.contentLineCount").value("1"))
            .andExpect(jsonPath("$.content[0]").value("Line 3"));
  }

  @Test
  public void log_startingAfterLastLine() throws Exception {
    String logPath = TEST_TMP_DIR + "/test.log";
    this.mockMvc.perform(get("/log?path=" + logPath + "&startAtLine=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(logPath))
            .andExpect(jsonPath("$.contentLineCount").value("0"))
            .andExpect(jsonPath("$.content").isEmpty());
  }

  @Test
  public void log_withNoDotExtension() throws Exception {
    String logPath = TEST_TMP_DIR + "/testlog";
    this.mockMvc.perform(get("/log?path=" + logPath))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(logPath))
            .andExpect(jsonPath("$.contentLineCount").value("3"))
            .andExpect(jsonPath("$.content[0]").value("Alfa"))
            .andExpect(jsonPath("$.content[1]").value("Bravo"))
            .andExpect(jsonPath("$.content[2]").value("Charlie"));
  }

  @Test
  public void log_withTxtExtension() throws Exception {
    String logPath = TEST_TMP_DIR + "/subdir-1/subdir-2/test.txt";
    this.mockMvc.perform(get("/log?path=" + logPath))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(logPath))
            .andExpect(jsonPath("$.contentLineCount").value("3"))
            .andExpect(jsonPath("$.content[0]").value("One"))
            .andExpect(jsonPath("$.content[1]").value("Two"))
            .andExpect(jsonPath("$.content[2]").value("Three"));
  }

  @Test
  public void log_exceptions() throws Exception {
    String logPath = System.getProperty("user.dir") + "/build.gradle";
    this.mockMvc.perform(get("/log?path=" + logPath))
            .andExpect(status().isForbidden())
            .andExpect(status().reason(endsWith("ALLOWED_LOG_DIRS")));

    logPath = TEST_TMP_DIR;
    this.mockMvc.perform(get("/log?path=" + logPath))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(status().reason(startsWith("Not a file")));

    logPath = TEST_TMP_DIR + "/subdir-1/subdir-2/testtxt";
    this.mockMvc.perform(get("/log?path=" + logPath))
            .andExpect(status().isForbidden())
            .andExpect(status().reason(endsWith("ALLOWED_LOG_EXTENSIONS")));

    logPath = TEST_TMP_DIR + "/dne.log";
    this.mockMvc.perform(get("/log?path=" + logPath))
            .andExpect(status().isNotFound())
            .andExpect(status().reason(endsWith("does not exist")));
  }

  @Test
  public void logDir_withLogsAndSubDirs() throws Exception {
    String logDirPath = TEST_TMP_DIR;
    this.mockMvc.perform(get("/logDir?path=" + logDirPath))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(logDirPath))
            .andExpect(jsonPath("$.logs[0]").value("test.log"))
            .andExpect(jsonPath("$.logs[1]").value("testlog"))
            .andExpect(jsonPath("$.subDirs[0].name").value("subdir-1"))
            .andExpect(jsonPath("$.subDirs[0].logs").isEmpty())
            .andExpect(jsonPath("$.subDirs[0].subDirs[0].name").value("subdir-2"))
            .andExpect(jsonPath("$.subDirs[0].subDirs[0].logs[0]").value("test.txt"))
            .andExpect(jsonPath("$.subDirs[0].subDirs[0].subDirs").isEmpty());
  }

  @Test
  public void logDir_withNoLogsInFirstDir() throws Exception {
    String logDirPath = TEST_TMP_DIR + "/subdir-1";
    this.mockMvc.perform(get("/logDir?path=" + logDirPath))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("subdir-1"))
            .andExpect(jsonPath("$.logs").isEmpty())
            .andExpect(jsonPath("$.subDirs[0].name").value("subdir-2"))
            .andExpect(jsonPath("$.subDirs[0].logs[0]").value("test.txt"))
            .andExpect(jsonPath("$.subDirs[0].subDirs").isEmpty());
  }

  @Test
  public void logDir_withNoLogsOrSubDirs() throws Exception {
    String logDirPath = TEST_TMP_DIR + "/subdir-1/subdir-2/subdir-3";
    this.mockMvc.perform(get("/logDir?path=" + logDirPath))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("subdir-3"))
            .andExpect(jsonPath("$.logs").isEmpty())
            .andExpect(jsonPath("$.subDirs").isEmpty());
  }

  @Test
  public void logDir_exceptions() throws Exception {
    String logDirPath = System.getProperty("user.dir");
    this.mockMvc.perform(get("/logDir?path=" + logDirPath))
            .andExpect(status().isForbidden())
            .andExpect(status().reason(endsWith("ALLOWED_LOG_DIRS")));

    logDirPath = TEST_TMP_DIR + "/dne";
    this.mockMvc.perform(get("/logDir?path=" + logDirPath))
            .andExpect(status().isNotFound())
            .andExpect(status().reason(endsWith("does not exist")));

    logDirPath = TEST_TMP_DIR + "/test.log";
    this.mockMvc.perform(get("/logDir?path=" + logDirPath))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(status().reason(startsWith("Not a directory")));
  }

  @Test
  public void dockerLog_startingAtFirstLine() throws Exception {
    String containerId = "driftlog_test";
    this.mockMvc.perform(get("/dockerLog?containerId=" + containerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("docker " + containerId))
            .andExpect(jsonPath("$.contentLineCount").value("4"))
            .andExpect(jsonPath("$.content[0]").value("INFO:root:Line 1"))
            .andExpect(jsonPath("$.content[1]").value("INFO:root:Line 2"))
            .andExpect(jsonPath("$.content[2]").value("INFO:root:Line 3"))
            .andExpect(jsonPath("$.content[3]").value("INFO:root:Sleeping..."));
  }

  @Test
  public void dockerLog_startingAtLastLine() throws Exception {
    String containerId = "driftlog_test";
    this.mockMvc.perform(get("/dockerLog?containerId=" + containerId + "&startAtLine=3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("docker " + containerId))
            .andExpect(jsonPath("$.contentLineCount").value("1"))
            .andExpect(jsonPath("$.content[0]").value("INFO:root:Sleeping..."));
  }

  @Test
  public void dockerLog_startingAfterLastLine() throws Exception {
    String containerId = "driftlog_test";
    this.mockMvc.perform(get("/dockerLog?containerId=" + containerId + "&startAtLine=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("docker " + containerId))
            .andExpect(jsonPath("$.contentLineCount").value("0"))
            .andExpect(jsonPath("$.content").isEmpty());
  }

  @Test
  public void dockerLog_exceptions() throws Exception {
    String containerId = "dne";
    this.mockMvc.perform(get("/dockerLog?containerId=" + containerId))
            .andExpect(status().isNotFound())
            .andExpect(status().reason(endsWith("does not exist")));
  }

  @Test
  public void dockerContainers_runningOnly() throws Exception {
    this.mockMvc.perform(get("/dockerContainers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].Names[0]").value("/" + TEST_DOCKER_CONTAINER + "3"))
            .andExpect(jsonPath("$[1].Names[0]").value("/" + TEST_DOCKER_CONTAINER + "2"))
            .andExpect(jsonPath("$[2].Names[0]").value("/" + TEST_DOCKER_CONTAINER));
  }

  @Test
  public void dockerContainers_inAnyState() throws Exception {
    this.mockMvc.perform(get("/dockerContainers?allContainers=true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(4)))
            .andExpect(jsonPath("$[0].Names[0]").value("/" + TEST_DOCKER_CONTAINER + "4"))
            .andExpect(jsonPath("$[1].Names[0]").value("/" + TEST_DOCKER_CONTAINER + "3"))
            .andExpect(jsonPath("$[2].Names[0]").value("/" + TEST_DOCKER_CONTAINER + "2"))
            .andExpect(jsonPath("$[3].Names[0]").value("/" + TEST_DOCKER_CONTAINER));
  }

  @Test
  public void dockerContainers_matchingOneLabel() throws Exception {
    String label = "someLabelKey=someLabelValue";
    this.mockMvc.perform(get("/dockerContainers?labels=" + label))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].Names[0]").value("/" + TEST_DOCKER_CONTAINER + "3"))
            .andExpect(jsonPath("$[1].Names[0]").value("/" + TEST_DOCKER_CONTAINER + "2"));
  }

  @Test
  public void dockerContainers_matchingTwoLabels() throws Exception {
    String labels = "someLabelKey=someLabelValue,anotherLabelKey=anotherLabelValue";
    this.mockMvc.perform(get("/dockerContainers?labels=" + labels))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].Names[0]").value("/" + TEST_DOCKER_CONTAINER + "3"));
  }

}
