package com.example.dependencies.controller;

import com.example.dependencies.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for UserController demonstrating validation testing
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_ValidUser_ReturnsCreated() throws Exception {
        UserDTO userDTO = UserDTO.builder()
                .username("johndoe")
                .email("john@example.com")
                .bio("Software developer")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("Johndoe")); // Capitalized
    }

    @Test
    void createUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        UserDTO userDTO = UserDTO.builder()
                .username("johndoe")
                .email("invalid-email")
                .bio("Software developer")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_MissingUsername_ReturnsBadRequest() throws Exception {
        UserDTO userDTO = UserDTO.builder()
                .email("john@example.com")
                .bio("Software developer")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_ShortUsername_ReturnsBadRequest() throws Exception {
        UserDTO userDTO = UserDTO.builder()
                .username("ab") // Less than 3 characters
                .email("john@example.com")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }
}
