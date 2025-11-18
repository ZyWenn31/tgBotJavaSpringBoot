package com.enduro.test.enduroTest.service;

import com.enduro.test.enduroTest.model.EnduroEntity;
import com.enduro.test.enduroTest.repository.EnduroEntityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnduroEntityService {
    private final EnduroEntityRepository enduroEntityRepository;

    public EnduroEntityService(EnduroEntityRepository enduroEntityRepository) {
        this.enduroEntityRepository = enduroEntityRepository;
    }

    public List<EnduroEntity> findAll(){
        return enduroEntityRepository.findAll();
    }

    public EnduroEntity findById(Long id){
        return enduroEntityRepository.findById(id).orElse(null);
    }

    public EnduroEntity save(EnduroEntity enduroEntity){
        return enduroEntityRepository.save(enduroEntity);
    }

    public void delete(Long id){
        enduroEntityRepository.deleteById(id);
    }

    public EnduroEntity findByName(String name){
        return enduroEntityRepository.findByName(name).orElse(null);
    }
}
