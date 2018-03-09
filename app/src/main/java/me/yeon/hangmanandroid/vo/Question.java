package me.yeon.hangmanandroid.vo;

/**
 * Created by yeon on 2018-03-08 008.
 */

import java.util.Arrays;
import java.util.Calendar;

public class Question {
    private String word;
    private String[] questions;
    private String[] answers;
    private int length;
    private Calendar beginDate;
    private Calendar dueDate;
    private Calendar reportDate;
    @Override
    public String toString() {
        return "Question [word=" + word + ", questions=" + Arrays.toString(questions) + ", answers="
                + Arrays.toString(answers) + ", length=" + length + ", beginDate=" + beginDate + ", dueDate=" + dueDate
                + ", reportDate=" + reportDate + "]";
    }
    public String getWord() {
        return word;
    }
    public void setWord(String word) {
        this.word = word;
    }
    public String[] getQuestions() {
        return questions;
    }
    public void setQuestions(String[] questions) {
        this.questions = questions;
    }
    public String[] getAnswers() {
        return answers;
    }
    public void setAnswers(String[] answers) {
        this.answers = answers;
    }
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
    public Calendar getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Calendar beginDate) {
        this.beginDate = beginDate;
    }

    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
    }

    public void setReportDate(Calendar reportDate) {
        this.reportDate = reportDate;
    }

    public Calendar getDueDate() {
        return dueDate;
    }
    public Calendar getReportDate() {
        return reportDate;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null){
            if(obj instanceof Question) {
                if (((Question) obj).getWord().equals(this.getWord())) {
                    return true;
                }
            }
        }
        return false;
    }
}