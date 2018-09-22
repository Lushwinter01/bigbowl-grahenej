package com.bigbowl.graphenej.api;

import com.bigbowl.graphenej.AssetAmount;
import com.bigbowl.graphenej.RPC;
import com.bigbowl.graphenej.Transaction;
import com.bigbowl.graphenej.interfaces.WitnessResponseListener;
import com.bigbowl.graphenej.models.ApiCall;
import com.bigbowl.graphenej.models.BaseResponse;
import com.bigbowl.graphenej.models.Block;
import com.bigbowl.graphenej.models.WitnessResponse;
import com.bigbowl.graphenej.operations.CustomOperation;
import com.bigbowl.graphenej.operations.LimitOrderCreateOperation;
import com.bigbowl.graphenej.operations.TransferOperation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetBlock extends BaseGrapheneHandler {

    private final static int LOGIN_ID = 1;
    private final static int GET_DATABASE_ID = 2;
    private final static int GET_BLOCK_ID = 3;

    private long blockNumber;
    private WitnessResponseListener mListener;

    private int currentId = LOGIN_ID;

    private boolean mOneTime;

    public GetBlock(long blockNumber, boolean oneTime, WitnessResponseListener listener){
        super(listener);
        this.blockNumber = blockNumber;
        this.mOneTime = oneTime;
        this.mListener = listener;
    }

    public GetBlock(long blockNumber, WitnessResponseListener listener){
        this(blockNumber, true, listener);
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> loginParams = new ArrayList<>();
        loginParams.add(null);
        loginParams.add(null);
        ApiCall loginCall = new ApiCall(1, RPC.CALL_LOGIN, loginParams, RPC.VERSION, currentId);
        websocket.sendText(loginCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        System.out.println("<<< "+response);

        Gson gson = new Gson();
        BaseResponse baseResponse = gson.fromJson(response, BaseResponse.class);
        if(baseResponse.error != null){
            mListener.onError(baseResponse.error);
            if(mOneTime){
                websocket.disconnect();
            }
        } else {
            currentId++;
            ArrayList<Serializable> emptyParams = new ArrayList<>();
            if (baseResponse.id == LOGIN_ID) {
                ApiCall getDatabaseId = new ApiCall(1, RPC.CALL_DATABASE, emptyParams, RPC.VERSION, currentId);
                websocket.sendText(getDatabaseId.toJsonString());
            } else if (baseResponse.id == GET_DATABASE_ID) {
                Type ApiIdResponse = new TypeToken<WitnessResponse<Integer>>() {}.getType();
                WitnessResponse<Integer> witnessResponse = gson.fromJson(response, ApiIdResponse);
                Integer apiId = witnessResponse.result;

                ArrayList<Serializable> params = new ArrayList<>();
                String blockNum = String.format("%d", this.blockNumber);
                params.add(blockNum);

                ApiCall loginCall = new ApiCall(apiId, RPC.CALL_GET_BLOCK, params, RPC.VERSION, currentId);
                websocket.sendText(loginCall.toJsonString());
            } else if (baseResponse.id == GET_BLOCK_ID) {
                Type BlockResponse = new TypeToken<WitnessResponse<Block>>(){}.getType();
                gson = new GsonBuilder()
                        .registerTypeAdapter(Transaction.class, new Transaction.TransactionDeserializer())
                        .registerTypeAdapter(TransferOperation.class, new TransferOperation.TransferDeserializer())
                        .registerTypeAdapter(LimitOrderCreateOperation.class, new LimitOrderCreateOperation.LimitOrderCreateDeserializer())
                        .registerTypeAdapter(CustomOperation.class, new CustomOperation.CustomOperationDeserializer())
                        .registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetAmountDeserializer())
                        .create();
                WitnessResponse<Block> blockResponse = gson.fromJson(response, BlockResponse);
                mListener.onSuccess(blockResponse);
                if (mOneTime) {
                    websocket.disconnect();
                }
            }
        }

    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            System.out.println(">>> "+frame.getPayloadText());
    }
}
