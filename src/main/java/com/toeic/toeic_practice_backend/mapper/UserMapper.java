package com.toeic.toeic_practice_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface UserMapper {
	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
	
//	@Mapping(target = "updatedBy", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "refreshToken", ignore = true)
//    @Mapping(target = "createdBy", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "id", ignore = true)
//	User toUserFromUserCreationRequest (UserCreationRequest userCreationRequest);
//	
//	UserCreationResponse toUserCreationResponseFromUser(User user);
	
}