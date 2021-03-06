package com.seandougnelson.driftlog;

import com.seandougnelson.driftlog.api.IDriftlog;
import com.seandougnelson.driftlog.api.Log;
import com.seandougnelson.driftlog.api.LogDir;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.messages.Container;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class DriftlogController {

  @Autowired
  private IDriftlog driftlog;

  @RequestMapping("/log")
  public Log getLog(@RequestParam String path, @RequestParam(defaultValue = "0") int startAtLine) {
    if (!driftlog.logDirIsAllowed(path)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "File '" + path + "' not in ALLOWED_LOG_DIRS");
    }
    if (Paths.get(path).toFile().isDirectory()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Not a file: '" + path + "'");
    }
    if (!driftlog.logExtensionIsAllowed(path)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "File extension of '" + path + "' not in " +
              "ALLOWED_LOG_EXTENSIONS");
    }

    try {
      return driftlog.getLog(path, startAtLine);

    } catch (NoSuchFileException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File '" + path + "' does not exist", e);
    } catch (Exception e) {
      throw new UnexpectedException(e);
    }
  }

  @RequestMapping("/logDir")
  public LogDir getLogDir(@RequestParam String path) {
    if (!driftlog.logDirIsAllowed(path)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Directory '" + path + "' not in ALLOWED_LOG_DIRS");
    }

    try {
      return driftlog.getLogDir(path);

    } catch (NoSuchFileException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Directory '" + path + "' does not exist", e);
    } catch (NotDirectoryException e) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Not a directory: '" + path + "'", e);
    } catch (Exception e) {
      throw new UnexpectedException(e);
    }
  }

  @RequestMapping("/dockerLog")
  public Log getDockerLog(@RequestParam String containerId, @RequestParam(defaultValue = "0") int startAtLine) {
    try {
      return driftlog.getDockerLog(containerId, startAtLine);

    } catch (ContainerNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Docker container with ID '" + containerId + "' does " +
              "not exist", e);
    } catch (Exception e) {
      throw new UnexpectedException(e);
    }

  }

  @RequestMapping("/dockerContainers")
  public List<Container> getDockerContainers(@RequestParam(required = false) String labels,
                                             @RequestParam(defaultValue = "false") boolean allContainers) {
    try {
      return driftlog.getDockerContainers(labels, allContainers);

    } catch (Exception e) {
      throw new UnexpectedException(e);
    }
  }

}
