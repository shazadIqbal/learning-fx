package com.mls.learningfx.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String name;
    private String contact;
    private String email;
    private String password;

}
