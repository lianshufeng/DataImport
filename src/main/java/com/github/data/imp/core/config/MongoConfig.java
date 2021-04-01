package com.github.data.imp.core.config;

import com.github.data.imp.other.mongo.config.MongoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
//jpa必须
@EnableMongoRepositories("com.github.data.imp.core.dao")
@Import({MongoConfiguration.class})
public class MongoConfig {
}
