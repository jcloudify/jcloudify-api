package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.UserMapper;
import api.jcloudify.app.endpoint.rest.model.CreateUsersRequestBody;
import api.jcloudify.app.endpoint.rest.model.CreateUsersResponse;
import api.jcloudify.app.service.UserService;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserController {
  private final UserService service;
  private final UserMapper mapper;

  @PostMapping("/users")
  public CreateUsersResponse signUp(@RequestBody CreateUsersRequestBody toCreate) {
    var data =
        service.createUsers(Objects.requireNonNull(toCreate.getData())).stream()
            .map(mapper::toRest)
            .toList();
    return new CreateUsersResponse().data(data);
  }
}
