package com.example.dependencies.mapper;

import com.example.dependencies.dto.UserDTO;
import com.example.dependencies.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for User entity and UserDTO
 * Demonstrates MapStruct integration with Spring and Lombok
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    /**
     * Convert User entity to UserDTO
     */
    UserDTO toDto(User user);

    /**
     * Convert UserDTO to User entity
     */
    User toEntity(UserDTO userDTO);

    /**
     * Update existing User entity from UserDTO
     * Ignores null values in the DTO
     */
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(UserDTO userDTO, @MappingTarget User user);
}
