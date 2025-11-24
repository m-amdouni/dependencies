package com.example.dependencies.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Entity demonstrating Lombok usage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String username;
    private String email;
    private String bio;
}
