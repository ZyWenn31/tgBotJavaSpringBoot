package com.enduro.test.enduroTest.model;

import jakarta.persistence.*;

@Entity(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private int grade;

    @ManyToOne
    @JoinColumn(name = "enduro_id", referencedColumnName = "id")
    private EnduroEntity enduro;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "chatId")
    private User user;


    public Feedback() {
    }

    public Feedback(String content, int grade, EnduroEntity enduro, User user) {
        this.content = content;
        this.grade = grade;
        this.enduro = enduro;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public EnduroEntity getEnduro() {
        return enduro;
    }

    public void setEnduro(EnduroEntity enduro) {
        this.enduro = enduro;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
