package com.tinmegali.springrestoauthandroidclient.api.errors;

import com.tinmegali.springrestoauthandroidclient.models.ErrorHttp;

/**
 * Created by tinmegali on 07/11/16.
 */
public class RestHttpException extends Exception {

    private ErrorHttp errorHttp;

    public RestHttpException(ErrorHttp errorHttp) {
        super("HttpError code["+errorHttp.getStatus()+"] " + errorHttp.getError());
        this.errorHttp = errorHttp;

    }

    public ErrorHttp getErrorHttp() {
        return errorHttp;
    }
}
