package com.ejiopeg.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ejiopeg.model.User;

public interface UserRepository extends MongoRepository<User, String> {

	public User findByUsername(String userName);
}
