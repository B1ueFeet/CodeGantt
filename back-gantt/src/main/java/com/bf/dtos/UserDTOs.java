package com.bf.dtos;

import com.bf.Model.UserRole;

import java.util.UUID;

public class UserDTOs {

    public record UserCreateRequest(
            UserRole role,
            String firstName,
            String lastName,
            String username,
            String email,
            String password 
    ) {}

    public record UserUpdateRequest(
            UserRole role,
            String firstName,
            String lastName,
            String email
    ) {}

    public record UserResponse(
            UUID id,
            UserRole role,
            String firstName,
            String lastName,
            String username,
            String email
    ) {}
}
