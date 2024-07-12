package api.jcloudify.app.endpoint.rest.converter;

import api.jcloudify.app.model.BoundedPageSize;
import org.springframework.core.convert.converter.Converter;

public class PageSizeConverter implements Converter<String, BoundedPageSize> {

  @Override
  public BoundedPageSize convert(String source) {
    return new BoundedPageSize(Integer.parseInt(source));
  }
}
