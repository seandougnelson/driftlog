package com.seandougnelson.driftlog;

import com.seandougnelson.driftlog.api.IDriftlog;
import com.seandougnelson.driftlog.api.Log;
import com.seandougnelson.driftlog.api.LogDir;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;

@RestController
public class DriftlogController {

  @Autowired
  private IDriftlog driftlog;

  @RequestMapping("/log")
  public Log getLog(@RequestParam String path, @RequestParam(defaultValue = "0") int startAtLine) throws IOException {
    try {
      return driftlog.getLog(path, startAtLine);

    } catch (NoSuchFileException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File '" + path + "' does not exist", e);
    } catch (AccessDeniedException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No read access to file '" + path + "'", e);
    }
  }

  @RequestMapping("/dockerLog")
  public Log getDockerLog(@RequestParam String containerId, @RequestParam(defaultValue = "0") int startAtLine)
          throws DockerException, InterruptedException {
    try {
      return driftlog.getDockerLog(containerId, startAtLine);

    } catch (ContainerNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Docker container with ID '" + containerId + "' does " +
              "not exist", e);
    }

  }

  @RequestMapping("/logDir")
  public LogDir getLogDir(@RequestParam String path) throws IOException {
    try {
      return driftlog.getLogDir(path);

    } catch (NoSuchFileException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Directory '" + path + "' does not exist", e);
    } catch (AccessDeniedException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No read access to directory '" + path + "'", e);
    }
  }

}
