package com.enduro.test.enduroTest.service;

import com.enduro.test.enduroTest.model.Feedback;
import com.enduro.test.enduroTest.model.User;
import com.enduro.test.enduroTest.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public User findById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    public User save(User user){
        return userRepository.save(user);
    }

    public void delete(Long id){
        userRepository.deleteById(id);
    }

    public List<User> findAllByRole(String role){
        return userRepository.findAllByRole(role);
    }
}
