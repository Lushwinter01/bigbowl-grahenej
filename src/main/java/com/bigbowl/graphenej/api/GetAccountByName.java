package com.bigbowl.graphenej.api;

import com.bigbowl.graphenej.AccountOptions;
import com.bigbowl.graphenej.Authority;
import com.bigbowl.graphenej.RPC;
import com.bigbowl.graphenej.interfaces.WitnessResponseListener;
import com.bigbowl.graphenej.models.AccountProperties;
import com.bigbowl.graphenej.models.ApiCall;
import com.bigbowl.graphenej.models.WitnessResponse;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  Class that implements get_account_by_name request handler.
 *
 *  Get an account’s info by name.
 *
 *  The response returns account data that refer to the name.
 *
 *  @see <a href="https://goo.gl/w75qjV">get_account_by_name API doc</a>
 */
public class GetAccountByName extends BaseGrapheneHandler {

    private String accountName;
    private WitnessResponseListener mListener;
    private boolean mOneTime;

    /**
     * Default Constructor
     *
     * @param accountName   name of the account to get info
     * @param oneTime       boolean value indicating if WebSocket must be closed (true) or not
     *                      (false) after the response
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetAccountByName(String accountName, boolean oneTime, WitnessResponseListener listener){
        super(listener);
        this.accountName = accountName;
        this.mOneTime = oneTime;
        this.mListener = listener;
    }

    /**
     * Using this constructor the WebSocket connection closes after the response.
     *
     * @param accountName   name of the account to get info
     * @param listener      A class implementing the WitnessResponseListener interface. This should
     *                      be implemented by the party interested in being notified about the
     *                      success/failure of the operation.
     */
    public GetAccountByName(String accountName, WitnessResponseListener listener){
        this(accountName, true, listener);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add(this.accountName);
        ApiCall getAccountByName = new ApiCall(0, RPC.CALL_GET_ACCOUNT_BY_NAME, accountParams, RPC.VERSION, 1);
        websocket.sendText(getAccountByName.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            System.out.println("<<< "+frame.getPayloadText());
        String response = frame.getPayloadText();
        GsonBuilder builder = new GsonBuilder();

        Type GetAccountByNameResponse = new TypeToken<WitnessResponse<AccountProperties>>(){}.getType();
        builder.registerTypeAdapter(Authority.class, new Authority.AuthorityDeserializer());
        builder.registerTypeAdapter(AccountOptions.class, new AccountOptions.AccountOptionsDeserializer());
        WitnessResponse<AccountProperties> witnessResponse = builder.create().fromJson(response, GetAccountByNameResponse);

        if(witnessResponse.error != null){
            this.mListener.onError(witnessResponse.error);
        }else{
            this.mListener.onSuccess(witnessResponse);
        }
        if(mOneTime){
            websocket.disconnect();
        }
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame()){
            System.out.println(">>> "+frame.getPayloadText());
        }
    }
}
