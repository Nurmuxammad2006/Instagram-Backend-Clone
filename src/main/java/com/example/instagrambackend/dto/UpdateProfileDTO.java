package com.example.instagrambackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileDTO {
    private String name;
    private String surname;
    private String username;
    private String bio;
    private String gender;
    private LocalDate birthDate;
}

