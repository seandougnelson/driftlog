package com.seandougnelson.driftlog.api;

import java.util.Set;
import java.util.TreeSet;

public class LogDir implements Comparable<LogDir> {
  String name;
  private Set<LogDir> subDirs;
  private Set<String> logs;

  public LogDir(String name) {
    this.name = name;
    this.subDirs = new TreeSet<>();
    this.logs = new TreeSet<>();
  }

  public String getName() {
    return name;
  }

  public Set<LogDir> getSubDirs() {
    return subDirs;
  }

  public Set<String> getLogs() {
    return logs;
  }

  public void addSubDir(LogDir subDir) {
    subDirs.add(subDir);
  }

  public void addLog(String log) {
    logs.add(log);
  }

  @Override
  public int compareTo(LogDir l) {
    return this.name.compareTo(l.getName());
  }
}
