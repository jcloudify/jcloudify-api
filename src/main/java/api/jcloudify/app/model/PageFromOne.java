package api.jcloudify.app.model;

import api.jcloudify.app.model.exception.BadRequestException;
import lombok.Getter;

public class PageFromOne {

  @Getter private final int value;

  public PageFromOne(int value) {
    if (value < 1) {
      throw new BadRequestException("page value must be >= 1");
    }
    this.value = value;
  }
}
