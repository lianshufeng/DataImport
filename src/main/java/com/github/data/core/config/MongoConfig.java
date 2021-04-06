package com.github.data.core.config;

import com.github.data.other.mongo.config.MongoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
//jpa必须
@EnableMongoRepositories("com.github.data.core.dao")
@Import({MongoConfiguration.class})
public class MongoConfig {
}
