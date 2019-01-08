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
public class WinnerProfile {

    private String userName;
    private String name;
    private String winningNumber;
    private String winningDate;
    private int edition;
    private Payment payment;
}
