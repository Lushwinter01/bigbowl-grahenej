package com.bigbowl.graphenej.models;

import java.io.Serializable;

import com.bigbowl.graphenej.GrapheneObject;
import com.bigbowl.graphenej.Transaction;

/**
 * Created by nelson on 1/28/17.
 */
public class BroadcastedTransaction extends GrapheneObject implements Serializable {
    public static final String KEY_TRX = "trx";
    public static final String KEY_TRX_ID = "trx_id";

    private Transaction trx;
    private String trx_id;

    public BroadcastedTransaction(String id){
        super(id);
    }

    public void setTransaction(Transaction t){
        this.trx = t;
    }

    public Transaction getTransaction() {
        return trx;
    }

    public void setTransactionId(String id){
        this.trx_id = id;
    }

    public String getTransactionId() {
        return trx_id;
    }
}
