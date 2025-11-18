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
}
