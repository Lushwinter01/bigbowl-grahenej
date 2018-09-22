package com.bigbowl.graphenej.errors;

import com.bigbowl.graphenej.api.BaseGrapheneHandler;

/**
 * Thrown by the {@link com.bigbowl.graphenej.api.SubscriptionMessagesHub#addRequestHandler(BaseGrapheneHandler)}
 * whenever the user tries to register a new handler with a previously registered id
 */

public class RepeatedRequestIdException extends Exception {
    public RepeatedRequestIdException(String message){
        super(message);
    }
}
