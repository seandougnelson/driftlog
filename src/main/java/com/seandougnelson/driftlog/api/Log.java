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

  public void setName(String name) {
    this.name = name;
  }

  public String[] getContent() {
    return content;
  }

  public void setContent(String[] content) {
    this.content = content;
  }

  public int getContentLineCount() {
    return contentLineCount;
  }

}
