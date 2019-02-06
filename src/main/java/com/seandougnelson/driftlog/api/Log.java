package com.seandougnelson.driftlog.api;

public class Log {
  private String name;
  private String[] content;
  private int contentLineCount;

  public Log(String name, String[] content) {
    this.name = name;
    this.content = content;
    this.contentLineCount = content.length;
  }

  public String getName() {
    return name;
  }

  public String[] getContent() {
    return content;
  }

  public int getContentLineCount() {
    return contentLineCount;
  }

}
