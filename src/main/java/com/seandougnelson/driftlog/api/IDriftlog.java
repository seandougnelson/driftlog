package com.seandougnelson.driftlog.api;

public interface IDriftlog {

  Log getLog(String filePath, int startAtLine);
  Log getDockerLog(String containerId, int startAtLine);
  LogDir getLogDir(String directoryPath);

}
