package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.BillingInfoMapper;
import api.jcloudify.app.endpoint.rest.mapper.UserMapper;
import api.jcloudify.app.endpoint.rest.model.BillingInfo;
import api.jcloudify.app.endpoint.rest.model.CreateUsersRequestBody;
import api.jcloudify.app.endpoint.rest.model.CreateUsersResponse;
import api.jcloudify.app.service.BillingInfoService;
import api.jcloudify.app.service.UserService;
import java.time.Instant;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserController {
  private final UserService service;
  private final UserMapper mapper;
  private final BillingInfoService billingInfoService;
  private final BillingInfoMapper billingInfoMapper;

  @PostMapping("/users")
  public CreateUsersResponse signUp(@RequestBody CreateUsersRequestBody toCreate) {
    var data =
        service.createUsers(Objects.requireNonNull(toCreate.getData())).stream()
            .map(mapper::toRest)
            .toList();
    return new CreateUsersResponse().data(data);
  }

  @GetMapping("/users/{userId}/billing")
  public BillingInfo getUserBillingInfo(
      @PathVariable String userId, @RequestParam Instant startTime, @RequestParam Instant endTime) {
    return billingInfoMapper.toRest(
        billingInfoService.getUserBillingInfo(userId, startTime, endTime), startTime, endTime);
  }
}
