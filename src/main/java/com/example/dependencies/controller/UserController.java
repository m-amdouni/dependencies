package com.example.dependencies.controller;

import com.example.dependencies.dto.UserDTO;
import com.example.dependencies.mapper.UserMapper;
import com.example.dependencies.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * User Controller demonstrating validation, Lombok, MapStruct, and Apache Commons usage
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserMapper userMapper;
    private final Map<Long, User> userStore = new HashMap<>();
    private Long idCounter = 1L;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        log.info("Creating user: {}", userDTO.getUsername());

        // Demonstrate Apache Commons Lang3 usage
        if (StringUtils.isBlank(userDTO.getUsername())) {
            return ResponseEntity.badRequest().build();
        }

        // Use MapStruct to convert DTO to Entity
        User user = userMapper.toEntity(userDTO);
        user.setId(idCounter++);

        // Capitalize username using Apache Commons
        user.setUsername(StringUtils.capitalize(user.getUsername()));

        userStore.put(user.getId(), user);

        // Convert back to DTO
        UserDTO responseDTO = userMapper.toDto(user);

        log.info("User created successfully with ID: {}", user.getId());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);

        User user = userStore.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        UserDTO userDTO = userMapper.toDto(user);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        log.info("Updating user with ID: {}", id);

        User existingUser = userStore.get(id);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }

        // Use MapStruct to update entity from DTO
        userMapper.updateEntityFromDto(userDTO, existingUser);

        UserDTO responseDTO = userMapper.toDto(existingUser);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);

        if (userStore.remove(id) == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
