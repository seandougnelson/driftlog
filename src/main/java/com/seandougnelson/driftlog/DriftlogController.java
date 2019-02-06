package com.seandougnelson.driftlog;

import com.seandougnelson.driftlog.api.IDriftlog;
import com.seandougnelson.driftlog.api.Log;
import com.seandougnelson.driftlog.api.LogDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DriftlogController {

  @Autowired
  private IDriftlog driftlog;

  @RequestMapping("/log")
  public Log getLog(@RequestParam String path, @RequestParam(defaultValue = "0") int startAtLine) {
    return driftlog.getLog(path, startAtLine);
  }

  @RequestMapping("/dockerLog")
  public Log getDockerLog(@RequestParam String containerId, @RequestParam(defaultValue = "0") int startAtLine) {
    return driftlog.getDockerLog(containerId, startAtLine);
  }

  @RequestMapping("/logDir")
  public LogDir getLogDir(@RequestParam String path) {
    return driftlog.getLogDir(path);
  }

}
