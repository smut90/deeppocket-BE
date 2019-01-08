package com.deep.pocket.model.response;

import com.deep.pocket.model.dao.Account;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class WinningNumber {

    @JsonProperty("edition")
    private int edition;

    @JsonProperty("editionFrom")
    private String editionFrom;

    @JsonProperty("editionTo")
    private String editionTo;

    @JsonProperty("winningNumber")
    private String number;

    @JsonProperty("winner")
    private Account account;
}
