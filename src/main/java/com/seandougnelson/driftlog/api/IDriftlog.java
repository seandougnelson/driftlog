package com.seandougnelson.driftlog.api;

import com.spotify.docker.client.exceptions.DockerException;

import java.io.IOException;

public interface IDriftlog {

  Log getLog(String filePath, int startAtLine) throws IOException;
  Log getDockerLog(String containerId, int startAtLine) throws DockerException, InterruptedException;
  LogDir getLogDir(String directoryPath) throws IOException;

}
