package com.learnk8s.app.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public abstract class DataRepository {

    @Autowired
    public MongoTemplate mongoTemplate;

}
