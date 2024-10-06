package com.toeic.toeic_practice_backend.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.toeic.toeic_practice_backend.utils.ZonedDateTimeReadConverter;
import com.toeic.toeic_practice_backend.utils.ZonedDateTimeWriteConverter;

@Configuration
public class DBConfig extends AbstractMongoClientConfiguration{

	@Value("${spring.data.mongodb.database}")
	private String dbName;
	@Value("${spring.data.mongodb.uri}")
	private String dbUri;
    
	@Override
	protected String getDatabaseName() {
		return this.dbName;
	}
	
	@Override
    public MongoClient mongoClient() {
        return MongoClients.create(this.dbUri);
    }
	
	@Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
	
	@Override
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new ZonedDateTimeReadConverter());
        converters.add(new ZonedDateTimeWriteConverter());
        return new MongoCustomConversions(converters);
    }

}