package me.yeon.hangmanandroid.vo;

/**
 * Created by yeon on 2018-03-07 007.
 */

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class User {
    private String name;
    private String id;

    private Calendar loginTime; //로그인 시각
    private Calendar answerTime; //가장 마지막에 푼 시각 (일정 시간 후 로그인 해제를 위함)

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Calendar getLoginTime() {
        return loginTime;
    }
    public void setLoginTime(Calendar loginTime) {
        this.loginTime = loginTime;
    }
    public Calendar getAnswerTime() {
        return answerTime;
    }
    public void setAnswerTime(Calendar answerTime) {
        this.answerTime = answerTime;
    }
    @Override
    public String toString() {
        return "User [name=" + name + ", loginTime=" + loginTime + "]";
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!User.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final User other = (User) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public User(){

    }

    public User(String id){
        this.id = id;
    }
}
