package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.UserMapper;
import api.jcloudify.app.endpoint.rest.model.User;
import api.jcloudify.app.endpoint.rest.model.UserBase;
import api.jcloudify.app.service.UserService;
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
  public User signUp(@RequestBody UserBase user) {
    return mapper.toRest(service.registerUser(user));
  }
}
