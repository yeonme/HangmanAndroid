package me.yeon.hangmanandroid.vo;

import me.yeon.hangmanandroid.comm.DataComm;

/**
 * Created by yeon on 2018-03-08 008.
 */

public class AsyncResult {
    private String result;
    private String requestUrl;
    private DataComm d;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public DataComm getDataComm() {
        return d;
    }

    public AsyncResult(DataComm d, String requestUrl, String result) {
        this.result = result;
        this.requestUrl = requestUrl;
        this.d = d;
    }
}
