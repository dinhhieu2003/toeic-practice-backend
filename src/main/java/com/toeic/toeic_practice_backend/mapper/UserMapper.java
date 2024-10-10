package com.toeic.toeic_practice_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import com.toeic.toeic_practice_backend.domain.dto.response.user.UserUpdateRoleResponse;
import com.toeic.toeic_practice_backend.domain.entity.User;

@Component
@Mapper(componentModel = "spring")
public interface UserMapper {
	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
	
//	User toUserFromUserCreationRequest (UserCreationRequest userCreationRequest);
//	
	UserUpdateRoleResponse toUserUpdateRoleResponseFromUser(User user);
	
}