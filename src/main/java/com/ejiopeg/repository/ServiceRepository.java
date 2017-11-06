package com.ejiopeg.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ejiopeg.model.Service;

public interface ServiceRepository extends MongoRepository<Service, String> {

	public Service findByServiceName(String serviceName);
}
