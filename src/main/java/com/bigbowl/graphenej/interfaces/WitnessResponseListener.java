package com.bigbowl.graphenej.interfaces;

import com.bigbowl.graphenej.models.BaseResponse;
import com.bigbowl.graphenej.models.WitnessResponse;

/**
 * Class used to represent any listener to network requests.
 */
public interface WitnessResponseListener {

    void onSuccess(WitnessResponse response);

    void onError(BaseResponse.Error error);
}
