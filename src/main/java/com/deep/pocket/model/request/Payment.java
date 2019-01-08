package com.deep.pocket.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel
public class Payment {

    @JsonProperty("payerEmail")
    private String payerEmail;

    @JsonProperty("payerId")
    private String payerId;

    @JsonProperty("paymentId")
    private String paymentId;

    @JsonProperty("paymentSuccess")
    private String paymentSuccess;

    @JsonProperty("ref")
    private String ref;
}
