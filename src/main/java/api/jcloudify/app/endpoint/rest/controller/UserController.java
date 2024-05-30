package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.UserMapper;
import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.model.User;
import api.jcloudify.app.service.UserService;
import java.util.List;
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
  public List<User> signUp(@RequestBody List<CreateUser> toCreate) {
    return service.createUsers(toCreate).stream().map(mapper::toRest).toList();
  }
}
