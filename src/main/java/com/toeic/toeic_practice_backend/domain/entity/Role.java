package com.toeic.toeic_practice_backend.domain.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "roles")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity{
    @Id
    private String id;
    private String name;
    private String description;
    private List<Permission> permissions = new ArrayList<>();
    @DBRef(lazy = false)
    private List<User> users = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permission {
    	private String name;
    	private String apiPath;
    	private String method;
    	private String module;
    }
}