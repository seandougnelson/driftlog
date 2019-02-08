package com.seandougnelson.driftlog;

import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class SimpleErrorController extends AbstractErrorController {

  public SimpleErrorController(ErrorAttributes errorAttributes) {
    super(errorAttributes);
  }

  @RequestMapping("/error")
  public Map<String, Object> handleError(HttpServletRequest request) {
    Map<String, Object> errorAttributes = super.getErrorAttributes(request, true);
    return errorAttributes;
  }

  @Override
  public String getErrorPath() {
    return "/error";
  }

}
