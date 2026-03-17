package service;

import dto.CustomerDTO;
import dto.UpdateProfileDTO;
import dto.UserRegisterDTO;
import dto.UserSessionDTO;
import entity.UserCategory;
import exception.EmailAlreadyExistsException;
import entity.User;
import mapper.UserMapper;
import org.mapstruct.factory.Mappers;
import repository.UserRepository;
import repository.impl.UserRepositoryImpl;
import util.HashUtil;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.stream.Collectors;

public class UserService extends BaseService<User> {

    private final UserRepository userRepository;

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    private final CategoryService categoryService = new CategoryService();

    public UserService() {
        this(new UserRepositoryImpl());
    }

    public UserService(UserRepository userRepository) {
        super(userRepository);
        this.userRepository = userRepository;
    }

    public Optional<User> getUserByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    public void register(UserRegisterDTO dto) throws EmailAlreadyExistsException {
        if (getUserByEmail(dto.email()).isPresent()) {
            throw new EmailAlreadyExistsException(dto.email());
        }

        User user = mapper.toEntity(dto);
        user.setPasswordHash(HashUtil.hashPassword(dto.passwordHash()));

        // Map category IDs → UserCategory manually
        if (dto.interests() != null) {
            for (Integer categoryId : dto.interests()) {
                categoryService.getById(categoryId)
                        .ifPresent(user::addInterest);
            }
        }
        userRepository.save(user);
    }

    public Optional<UserSessionDTO> login(String email, String password) {
        Optional<User> userOpt = getUserByEmail(email);

        if (userOpt.isEmpty()) return Optional.empty();

        User user = userOpt.get();
        if (!HashUtil.verifyPassword(password, user.getPasswordHash())) {
            return Optional.empty();
        }

        // Force load lazy collections while EntityManager is still open
        user.getInterests().forEach(uc -> uc.getCategory().getName());

        user.getPaymentCards().size();

        return Optional.of(mapper.toSessionDTO(user));
    }

    public UserSessionDTO updateProfile(Integer userId, UpdateProfileDTO dto)
            throws IllegalArgumentException {

        User user = getById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        /* ── Name ── */
        if (isBlank(dto.name())) {
            throw new IllegalArgumentException("Full name is required.");
        }
        user.setName(dto.name().trim());

        /* ── Email ── */
        if (isBlank(dto.email())) {
            throw new IllegalArgumentException("Email address is required.");
        }
        String newEmail = dto.email().trim().toLowerCase();
        if (!newEmail.equals(user.getEmail())) {
            if (getUserByEmail(newEmail).isPresent()) {
                throw new IllegalArgumentException("That email address is already in use.");
            }
            user.setEmail(newEmail);
        }

        /* ── Birthday ── */
        if (!isBlank(dto.birthday())) {
            try {
                user.setBirthday(LocalDate.parse(dto.birthday()));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date of birth format.");
            }
        } else {
            user.setBirthday(null);
        }

        /* ── Job ── */
        user.setJob(isBlank(dto.job()) ? null : dto.job().trim());

        /* ── Address ── */
        user.setAddress(isBlank(dto.address()) ? null : dto.address().trim());

        /* ── Interests ── */
        if (dto.interests() != null) {

            /* Get the category IDs the user currently has */
            Set<Integer> currentIds = user.getInterests()
                    .stream()
                    .map(uc -> uc.getCategory().getId())
                    .collect(Collectors.toSet());

            Set<Integer> newIds = new HashSet<>(dto.interests());

            /* Remove interests that are no longer selected */
            user.getInterests().removeIf(uc -> !newIds.contains(uc.getCategory().getId()));

            /* Add interests that are newly selected */
            for (Integer categoryId : newIds) {
                if (!currentIds.contains(categoryId)) {
                    categoryService.getById(categoryId)
                            .ifPresent(user::addInterest);
                }
            }
        }

        /* ── Password change (optional) ── */
        if (!isBlank(dto.newPassword())) {
            if (isBlank(dto.currentPassword())) {
                throw new IllegalArgumentException("Current password is required to set a new one.");
            }
            if (!HashUtil.verifyPassword(dto.currentPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Current password is incorrect.");
            }
            if (!dto.newPassword().equals(dto.confirmPassword())) {
                throw new IllegalArgumentException("New passwords do not match.");
            }
            if (dto.newPassword().length() < 8) {
                throw new IllegalArgumentException("New password must be at least 8 characters.");
            }
            user.setPasswordHash(HashUtil.hashPassword(dto.newPassword()));
        }

        /* ── Persist ── */
        User saved = userRepository.save(user);

        /* ── Force-load lazy collections for session DTO ── */
        saved.getInterests().forEach(uc -> uc.getCategory().getName());
        saved.getPaymentCards().size();

        return mapper.toSessionDTO(saved);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public List<CustomerDTO> getAllCustomer(){
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    user.getInterests().forEach(uc -> uc.getCategory().getName());
                    return mapper.toCustomerDto(user);
                })
                .collect(Collectors.toList());
    }
}