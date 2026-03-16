package service;

import dto.CustomerDTO;
import dto.UserRegisterDTO;
import dto.UserSessionDTO;
import exception.EmailAlreadyExistsException;
import entity.User;
import dto.UserProfileDTO;
import mapper.UserMapper;
import org.mapstruct.factory.Mappers;
import repository.UserRepository;
import repository.impl.UserRepositoryImpl;
import util.HashUtil;

import java.util.List;
import java.util.Optional;
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

        return Optional.of(mapper.toSessionDTO(user));
    }

    public Optional<UserProfileDTO> getProfileDetails(Integer id) {

        User user = userRepository.findById(id);

        return Optional.ofNullable(mapper.toProfileDto(user));
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