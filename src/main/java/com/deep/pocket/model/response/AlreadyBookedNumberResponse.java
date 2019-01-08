package com.deep.pocket.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class AlreadyBookedNumberResponse {

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("edition")
    private int edition;

    @JsonProperty("editionFrom")
    private String editionFrom;

    @JsonProperty("editionTo")
    private String editionTo;

    @JsonProperty("isBookingAllowed")
    private boolean isBookingAllowed;

    @JsonProperty("numbersAllowedToBook")
    private int numbersAllowedToBook;
}
