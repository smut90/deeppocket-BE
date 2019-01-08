package com.deep.pocket.model.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Payment {
    private String id;
    private String accId;
    private String dateAdded;
    private int edition;
    private String payerEmail;
    private String payerId;
    private String paymentId;
    private String paymentSuccess;
    private String purchasedNumberCount;
}
