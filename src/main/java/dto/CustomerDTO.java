package dto;

import entity.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record CustomerDTO(
        Integer id,
        String name,
        String email,
        Gender gender,
        LocalDate birthday,
        String job,
        String address,
        BigDecimal creditLimit,
        Set<UserInterestDTO> interests
) {}