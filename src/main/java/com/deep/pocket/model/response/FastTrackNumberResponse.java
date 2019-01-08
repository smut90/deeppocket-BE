package com.deep.pocket.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class FastTrackNumberResponse {

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("edition")
    private int edition;

    @JsonProperty("editionFrom")
    private String editionFrom;

    @JsonProperty("editionTo")
    private String editionTo;

    @JsonProperty("fastTrackNumbers")
    private Set<String> fastTrackNumbers;
}
