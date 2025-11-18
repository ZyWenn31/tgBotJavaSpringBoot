package com.enduro.test.enduroTest.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "enduro")
public class EnduroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private int cubes;

    private int tact;

    private int horsepower;

    private int weight;

    private int price;

    @OneToMany(mappedBy = "enduro", cascade = CascadeType.ALL)
    private List<Feedback> feedbacks = new ArrayList<>();

    public EnduroEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public int getCubes() {
        return cubes;
    }

    public void setCubes(int cubes) {
        this.cubes = cubes;
    }

    public int getTact() {
        return tact;
    }

    public void setTact(int tact) {
        this.tact = tact;
    }

    public int getHorsepower() {
        return horsepower;
    }

    public void setHorsepower(int horsepower) {
        this.horsepower = horsepower;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public EnduroEntity() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
