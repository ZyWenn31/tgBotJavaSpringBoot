package com.enduro.test.enduroTest.service;

import com.enduro.test.enduroTest.model.EnduroEntity;
import com.enduro.test.enduroTest.model.Feedback;
import com.enduro.test.enduroTest.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public List<Feedback> findAll(){
        return feedbackRepository.findAll();
    }

    public Feedback findById(Long id){
        return feedbackRepository.findById(id).orElse(null);
    }

    public Feedback save(Feedback feedback){
        return feedbackRepository.save(feedback);
    }

    public void delete(Long id){
        feedbackRepository.deleteById(id);
    }
}
