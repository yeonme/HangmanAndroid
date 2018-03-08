package me.yeon.hangmanandroid.vo;

/**
 * Created by yeon on 2018-03-08 008.
 */

public class Result {
    private Object data;
    private String status;
    private String desc;
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    @Override
    public String toString() {
        return "Result [data=" + data + ", status=" + status + ", desc=" + desc + "]";
    }
    public Result(String status, String desc, Object data) {
        this.data = data;
        this.status = status;
        this.desc = desc;
    }
}
