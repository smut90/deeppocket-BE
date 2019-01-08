package com.deep.pocket.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeepPocketException extends Exception  {

    public DeepPocketException(String message) {
        super(message);
    }

    public DeepPocketException(String message, Throwable t) {
        super(message, t);
    }
}
