package com.toeic.toeic_practice_backend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import com.toeic.toeic_practice_backend.domain.dto.response.user.UserInfoResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UserUpdateRoleResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.user.UserUpdateStatusResponse;
import com.toeic.toeic_practice_backend.domain.entity.User;

@Component
@Mapper(componentModel = "spring")
public interface UserMapper {
	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
	
//	User toUserFromUserCreationRequest (UserCreationRequest userCreationRequest);
//	
	UserUpdateRoleResponse toUserUpdateRoleResponseFromUser(User user);
	UserUpdateStatusResponse toUserUpdateStatusResponseFromUser(User user);
	UserInfoResponse toUserInfoResponseFromUser(User user);
	List<UserInfoResponse> toListUserInfoResponseFromListUser(List<User> listUser);
}