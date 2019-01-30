package com.seandougnelson.driftlog;

import com.seandougnelson.driftlog.api.IDriftlog;
import com.seandougnelson.driftlog.api.Log;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class DriftlogImpl implements IDriftlog {

  private DockerClient docker;

  public DriftlogImpl() {
    this.docker = new DefaultDockerClient("unix:///var/run/docker.sock");
  }

  public Log getLog(String fileName, int startAtLine) {
    String logName = fileName;

    String[] logContent;
    try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
      logContent = stream.skip(startAtLine).toArray(String[]::new);
    } catch (IOException e) {
      logContent = new String[]{"An error occurred while reading " + logName};
      e.printStackTrace();
    }

    return new Log(logName, logContent);
  }

  public Log getDockerLog(String containerId, int startAtLine) {
    String logName = "docker " + containerId;

    String content;
    try (LogStream stream = docker.logs(containerId, DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr())) {
      content = stream.readFully();
    } catch (Exception e) {
      content = "An error occurred while reading " + logName;
      e.printStackTrace();
    }

    String[] logContent = content.split("\\r?\\n");
    if (startAtLine != 0) {
      String[] newLogContent = new String[Math.max(0, logContent.length - startAtLine)];
      for (int i = startAtLine; i < logContent.length; i++) {
        newLogContent[i - startAtLine] = logContent[i];
      }
      return new Log(logName, newLogContent);
    }

    return new Log(logName, logContent);
  }

}
