package com.deep.pocket.model.response;

import com.deep.pocket.model.dao.Edition;
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
public class NextDraw {

    @JsonProperty("nextDraw")
    private String nextDrawDateTime;

    @JsonProperty("edition")
    private Edition edition;
}
