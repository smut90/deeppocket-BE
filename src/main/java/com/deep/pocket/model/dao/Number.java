package com.deep.pocket.model.dao;

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
public class Number {
    private String dateAdded;
    private String accId;
    private String number;
    private String paymentId;
    private int edition;
}
