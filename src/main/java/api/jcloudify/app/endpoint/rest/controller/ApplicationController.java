package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.ApplicationMapper;
import api.jcloudify.app.endpoint.rest.mapper.BillingInfoMapper;
import api.jcloudify.app.endpoint.rest.model.Application;
import api.jcloudify.app.endpoint.rest.model.BillingInfo;
import api.jcloudify.app.endpoint.rest.model.CrupdateApplicationsRequestBody;
import api.jcloudify.app.endpoint.rest.model.CrupdateApplicationsResponse;
import api.jcloudify.app.endpoint.rest.model.PagedApplicationsResponse;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.BillingInfoService;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {
  private final ApplicationService applicationService;
  private final ApplicationMapper mapper;
  private final BillingInfoService billingInfoService;
  private final BillingInfoMapper billingInfoMapper;

  public ApplicationController(
      ApplicationService applicationService,
      @Qualifier("RestApplicationMapper") ApplicationMapper mapper,
      BillingInfoService billingInfoService,
      BillingInfoMapper billingInfoMapper) {
    this.applicationService = applicationService;
    this.mapper = mapper;
    this.billingInfoService = billingInfoService;
    this.billingInfoMapper = billingInfoMapper;
  }

  @PutMapping("/users/{userId}/applications")
  public CrupdateApplicationsResponse crupdateApplications(
      @PathVariable String userId, @RequestBody CrupdateApplicationsRequestBody toCrupdate) {
    var data = Objects.requireNonNull(toCrupdate.getData());
    var mappedData =
        applicationService.saveApplications(data).stream().map(mapper::toRest).toList();
    return new CrupdateApplicationsResponse().data(mappedData);
  }

  @GetMapping("/users/{userId}/applications")
  public PagedApplicationsResponse getApplications(
      @PathVariable String userId,
      @RequestParam(required = false, defaultValue = "1") PageFromOne page,
      @RequestParam(required = false, defaultValue = "10") BoundedPageSize pageSize,
      @RequestParam(required = false) String name) {
    var pagedData = applicationService.findAllByCriteria(userId, name, page, pageSize);
    var mappedData = pagedData.data().stream().map(mapper::toRest).toList();
    return new PagedApplicationsResponse()
        .count(pagedData.count())
        .hasPrevious(pagedData.hasPrevious())
        .pageSize(pagedData.queryPageSize().getValue())
        .pageNumber(pagedData.queryPage().getValue())
        .data(mappedData);
  }

  @GetMapping("/users/{userId}/applications/{applicationId}")
  public Application getApplicationById(
      @PathVariable String userId, @PathVariable String applicationId) {
    return mapper.toRest(applicationService.getById(applicationId, userId));
  }

  @GetMapping("/users/{userId}/applications/{applicationId}/billing")
  public List<BillingInfo> getUserApplicationBillingInfo(
      @PathVariable String userId,
      @PathVariable String applicationId,
      @RequestParam Instant startTime,
      @RequestParam Instant endTime) {
    return billingInfoService
        .getUserBillingInfoByApplication(userId, applicationId, startTime, endTime)
        .stream()
        .map(billingInfo -> billingInfoMapper.toRest(billingInfo, startTime, endTime))
        .toList();
  }
}
