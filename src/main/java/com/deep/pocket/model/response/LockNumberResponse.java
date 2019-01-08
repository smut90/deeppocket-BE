package com.deep.pocket.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.ResourceSupport;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class LockNumberResponse extends ResourceSupport {

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("bookedNumbers")
    private Set<String> numbers;

    @JsonProperty("edition")
    private int edition;

    @JsonProperty("editionFrom")
    private String editionFrom;

    @JsonProperty("editionTo")
    private String editionTo;

    @JsonProperty("dimesPurchaseHistory")
    private String dimesPurchaseHistory;

    @JsonProperty("already_locked")
    private boolean alreadyLocked;

    @JsonProperty("locked_successful")
    private boolean locked;
}
