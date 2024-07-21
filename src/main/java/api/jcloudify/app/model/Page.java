package api.jcloudify.app.model;

import java.util.Collection;

public record Page<T>(PageFromOne queryPage, BoundedPageSize queryPageSize, Collection<T> data) {
  public boolean hasPrevious() {
    return queryPage.getValue() > 1;
  }

  public int count() {
    return data.size();
  }
}
