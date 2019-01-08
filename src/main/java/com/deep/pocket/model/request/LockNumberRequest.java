package com.deep.pocket.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@ApiModel
public class LockNumberRequest {

    @JsonProperty("user")
    private User user;

    @JsonProperty("payment")
    private Payment payment;

    @JsonProperty("purchasedNumbers")
    private List<String> numberList;

    @JsonProperty("purchasedNumberCount")
    private String purchasedNumberCount;

}
