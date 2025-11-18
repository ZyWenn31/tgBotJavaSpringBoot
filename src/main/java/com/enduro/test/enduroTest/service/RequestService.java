package com.enduro.test.enduroTest.service;

import com.enduro.test.enduroTest.model.Request;
import com.enduro.test.enduroTest.repository.RequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestService {

    private final RequestRepository repository;

    public RequestService(RequestRepository repository) {
        this.repository = repository;
    }

    public List<Request> findAll(){
        return repository.findAll();
    }

    public Request findById(Long id){
        return repository.findById(id).orElse(null);
    }

    public Request save(Request request){
        return repository.save(request);
    }

    public void delete(Long id){
        repository.deleteById(id);
    }

    public List<Request> findByCompleted(boolean completed){
        return repository.findAllByCompleted(completed);
    }
}
