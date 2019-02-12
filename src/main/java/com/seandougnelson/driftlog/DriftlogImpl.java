package com.seandougnelson.driftlog;

import com.seandougnelson.driftlog.api.IDriftlog;
import com.seandougnelson.driftlog.api.Log;
import com.seandougnelson.driftlog.api.LogDir;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Service
public class DriftlogImpl implements IDriftlog {

  private Logger logger = LoggerFactory.getLogger(DriftlogImpl.class);
  private DockerClient docker;
  private String[] allowedLogDirs;
  private String[] allowedLogExtensions;

  public DriftlogImpl() {
    docker = new DefaultDockerClient("unix:///var/run/docker.sock");
    try {
      docker.version();
    } catch (Exception e) {
      logger.error("Unable to connect to Docker (verify that Docker is running and 'docker.sock' is mounted to the " +
              "container)");
      DriftlogApplication.exit();
    }

    allowedLogDirs = Env.ALLOWED_LOG_DIRS.getValue().split(",");
    String extensions = Env.ALLOWED_LOG_EXTENSIONS.getValue();
    if (extensions != null) {
      allowedLogExtensions = extensions.split(",");
    } else {
      allowedLogExtensions = new String[]{".log"};
    }
  }

  public Log getLog(String filePath, int startAtLine) throws IOException {
    String logName = filePath;
    Stream<String> stream = Files.lines(Paths.get(filePath));
    String[] logContent = stream.skip(startAtLine).toArray(String[]::new);

    return new Log(logName, logContent);
  }

  public LogDir getLogDir(String dirPath) throws IOException {
    DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dirPath));

    LogDir logDir = new LogDir(Paths.get(dirPath).getFileName().toString());
    for (Path entry : stream) {
      if (entry.toFile().isDirectory()) {
        LogDir subDir = getLogDir(entry.toAbsolutePath().toString());
        if (subDir != null && (!subDir.getSubDirs().isEmpty() || !subDir.getLogs().isEmpty())) {
          logDir.addSubDir(subDir);
        }
      } else if (Arrays.stream(allowedLogExtensions).anyMatch(entry.toString()::endsWith)) {
        logDir.addLog(entry.getFileName().toString());
      }
    }

    return logDir;
  }

  public boolean logDirIsAllowed(String path) {
    return Arrays.stream(allowedLogDirs).anyMatch(path::startsWith);
  }

  public boolean logExtensionIsAllowed(String path) {
    return Arrays.stream(allowedLogExtensions).anyMatch(path::endsWith);
  }

  public Log getDockerLog(String containerId, int startAtLine) throws DockerException, InterruptedException {
    String logName = "docker " + containerId;
    LogStream stream = docker.logs(containerId, DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr());

    String[] logContent = stream.readFully().split("\\r?\\n");
    if (startAtLine != 0) {
      String[] newLogContent = new String[Math.max(0, logContent.length - startAtLine)];
      for (int i = startAtLine; i < logContent.length; i++) {
        newLogContent[i - startAtLine] = logContent[i];
      }
      return new Log(logName, newLogContent);
    }

    return new Log(logName, logContent);
  }

  public List<Container> getDockerContainers(String labels, boolean allContainers) throws DockerException,
          InterruptedException {
    List<DockerClient.ListContainersParam> params = new ArrayList<>();
    if (labels != null) {
      String[] labelsArray = labels.split(",");
      for (String label : labelsArray) {
        params.add(DockerClient.ListContainersParam.withLabel(label));

      }
    }
    if (allContainers) {
      params.add(DockerClient.ListContainersParam.allContainers());
    }

    return docker.listContainers(params.toArray(DockerClient.ListContainersParam[]::new));
  }

}
