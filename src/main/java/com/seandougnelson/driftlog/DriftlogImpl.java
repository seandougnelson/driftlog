package com.seandougnelson.driftlog;

import com.seandougnelson.driftlog.api.IDriftlog;
import com.seandougnelson.driftlog.api.Log;
import com.seandougnelson.driftlog.api.LogDir;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

@Service
public class DriftlogImpl implements IDriftlog {

  private DockerClient docker;
  private String[] logFileExtensions;

  public DriftlogImpl() {
    docker = new DefaultDockerClient("unix:///var/run/docker.sock");
    try {
      docker.info();
      docker.version();
    } catch (Exception e) {
      System.err.println("Unable to connect to Docker. Verify that:\n  1. Docker is running and listening on a unix " +
              "socket.\n  2. Unix socket is mounted to the container (e.g. 'docker run -v /var/run/docker" +
              ".sock:/var/run/docker.sock container').");
      System.exit(1);
    }

    String extensions = System.getenv("LOG_EXTENSIONS");
    if (extensions != null) {
      logFileExtensions = extensions.split(",");
    } else {
      logFileExtensions = new String[]{".log"};
    }
  }

  // TODO Only get logs from allowed dirs
  public Log getLog(String filePath, int startAtLine) throws IOException {
    String logName = filePath;
    Stream<String> stream = Files.lines(Paths.get(filePath));
    String[] logContent = stream.skip(startAtLine).toArray(String[]::new);

    return new Log(logName, logContent);
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

  // TODO Env to specify allowed dirs
  // TODO Env to toggle subdirs
  public LogDir getLogDir(String dirPath) throws IOException {
    DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dirPath));

    LogDir logDir = new LogDir(Paths.get(dirPath).getFileName().toString());
    for (Path entry : stream) {
      if (entry.toFile().isDirectory()) {
        LogDir subDir = getLogDir(entry.toAbsolutePath().toString());
        if (subDir != null && !subDir.getLogs().isEmpty()) {
          logDir.addSubDir(subDir);
        }
      } else if (Arrays.stream(logFileExtensions).anyMatch(entry.toString()::endsWith)) {
        logDir.addLog(entry.getFileName().toString());
      }
    }

    return logDir;
  }

}
