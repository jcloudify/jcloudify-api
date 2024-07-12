package api.jcloudify.app.endpoint.rest.converter;

import api.jcloudify.app.model.PageFromOne;
import org.springframework.core.convert.converter.Converter;

public class PageConverter implements Converter<String, PageFromOne> {

  @Override
  public PageFromOne convert(String source) {
    return new PageFromOne(Integer.parseInt(source));
  }
}
