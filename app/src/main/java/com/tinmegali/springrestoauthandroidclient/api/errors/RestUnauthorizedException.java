package com.tinmegali.springrestoauthandroidclient.api.errors;

import com.tinmegali.springrestoauthandroidclient.models.ErrorUnauthorized;

/**
 * Created by tinmegali on 07/11/16.
 */
public class RestUnauthorizedException extends Exception {

    private ErrorUnauthorized errorUnauthorized;

    public RestUnauthorizedException(ErrorUnauthorized errorUnauthorized) {
        super(errorUnauthorized.getErrorDescription() +":"+ errorUnauthorized.getErrorDescription());
        this.errorUnauthorized = errorUnauthorized;
    }

    public ErrorUnauthorized getErrorUnauthorized() {
        return errorUnauthorized;
    }
}
