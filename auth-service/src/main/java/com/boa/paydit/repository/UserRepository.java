package com.boa.paydit.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.boa.paydit.entity.UserEntity;

public interface UserRepository
extends MongoRepository<UserEntity,String>{

    Optional<UserEntity>
    findByUsername(String username);
}