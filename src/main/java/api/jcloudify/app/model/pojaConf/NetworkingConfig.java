package api.jcloudify.app.model.pojaConf;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NetworkingConfig(
    @JsonProperty("region") String region,
    @JsonProperty("with_own_vpc") boolean withOwnVpc,
    @JsonProperty("ssm_sg_id") String ssmSgId,
    @JsonProperty("ssm_subnet1_id") String ssmSubnet1Id,
    @JsonProperty("ssm_subnet2_id") String ssmSubnet2Id) {}
