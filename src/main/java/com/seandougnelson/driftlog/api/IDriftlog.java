package com.seandougnelson.driftlog.api;

public interface IDriftlog {

  public Log getLog(String fileName, int startAtLine);
  public Log getDockerLog(String containerId, int startAtLine);

}
