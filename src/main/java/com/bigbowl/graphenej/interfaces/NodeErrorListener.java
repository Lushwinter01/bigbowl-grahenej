package com.bigbowl.graphenej.interfaces;

import com.bigbowl.graphenej.models.BaseResponse;

/**
 * Interface to be implemented by any listener to network errors.
 */
public interface NodeErrorListener {
    void onError(BaseResponse.Error error);
}
