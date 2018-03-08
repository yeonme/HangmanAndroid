package me.yeon.hangmanandroid.vo;

/**
 * Created by yeon on 2018-03-08 008.
 */

/**
 * ResultEntry는 제출된 결과물의 기록을 나타낸다.
 * @author yeon
 *
 */
public class ResultEntry {
    //(중도참가yn, 시도 횟수, 오답 횟수, 기록된 ms, id)
    private boolean partial;
    private int tries;
    private int wrong;
    private int ms;
    private String id;
    private String name;
    private String device;
    public boolean isPartial() {
        return partial;
    }
    public void setPartial(boolean partial) {
        this.partial = partial;
    }
    public int getTries() {
        return tries;
    }
    public void setTries(int tries) {
        this.tries = tries;
    }
    public int getWrong() {
        return wrong;
    }
    public void setWrong(int wrong) {
        this.wrong = wrong;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDevice() {
        return device;
    }
    public void setDevice(String device) {
        this.device = device;
    }
    public int getMs() {
        return ms;
    }
    public void setMs(int ms) {
        this.ms = ms;
    }
    public ResultEntry(){

    }
    public ResultEntry(boolean partial, int tries, int wrong, String id, String name, String device) {
        super();
        this.partial = partial;
        this.tries = tries;
        this.wrong = wrong;
        this.id = id;
        this.name = name;
        this.device = device;
    }
    @Override
    public String toString() {
        return "ResultEntry [partial=" + partial + ", tries=" + tries + ", wrong=" + wrong + ", id=" + id + ", name="
                + name + ", device=" + device + "]";
    }

}