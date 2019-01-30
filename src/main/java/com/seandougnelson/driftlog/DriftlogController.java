package com.seandougnelson.driftlog;

import com.seandougnelson.driftlog.api.IDriftlog;
import com.seandougnelson.driftlog.api.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DriftlogController {

  @Autowired
  private IDriftlog driftlog;

  @RequestMapping("/log")
  public Log log(@RequestParam String fileName, @RequestParam(defaultValue = "0") int startAtLine) {
    return driftlog.getLog(fileName, startAtLine);
  }

  @RequestMapping("/dockerLog/{containerId}")
  public Log dockerLog(@PathVariable String containerId, @RequestParam(defaultValue = "0") int startAtLine) {
    return driftlog.getDockerLog(containerId, startAtLine);
  }

}
