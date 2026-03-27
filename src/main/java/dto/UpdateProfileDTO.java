package dto;

import java.util.Set;

public record UpdateProfileDTO(
        String name,
        String email,
        String birthday,
        String job,
        String address,
        Set<Integer> interests,
        String currentPassword,
        String newPassword,
        String confirmPassword
) {}
