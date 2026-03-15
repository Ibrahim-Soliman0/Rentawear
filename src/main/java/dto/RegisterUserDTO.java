package dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RegisterUserDTO(
        Integer id,
        String name,
        String email,
        String passwordHash,
        String gender,
        LocalDate birthday,
        String job,
        String address,
        BigDecimal creditLimit,
        List<Integer> interests
) {}