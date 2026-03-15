package dto;

import entity.enums.Gender;
import entity.enums.UserRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record UserSessionDTO(
        Integer id,
        String name,
        String email,
        Gender gender,
        LocalDate birthday,
        String job,
        String address,
        BigDecimal creditLimit,
        UserRole role,
        Set<UserInterestDTO> interests
) {}
