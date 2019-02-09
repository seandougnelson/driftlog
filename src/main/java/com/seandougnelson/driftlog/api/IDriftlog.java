package com.seandougnelson.driftlog.api;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;

import java.io.IOException;
import java.util.List;

public interface IDriftlog {

  Log getLog(String filePath, int startAtLine) throws IOException;
  LogDir getLogDir(String directoryPath) throws IOException;
  Log getDockerLog(String containerId, int startAtLine) throws DockerException, InterruptedException;
  List<Container> getDockerContainers(String labels, boolean allContainers) throws DockerException,
          InterruptedException;

}
