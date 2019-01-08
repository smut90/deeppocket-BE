package com.deep.pocket.model.response;

import com.deep.pocket.model.dao.Edition;
import com.deep.pocket.model.dao.Payment;
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
public class WinnerInfoForDashboard {

    private String name;
    private String winningNumber;
    private String winningDate;
    private Edition edition;

}
