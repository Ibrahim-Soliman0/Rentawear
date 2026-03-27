package service;

import entity.UserCategory;
import repository.UserCategoryRepository;
import repository.impl.UserCategoryRepositoryImpl;

public class UserCategoryService extends BaseService<UserCategory> {

    private final UserCategoryRepository userCategoryRepository;

    public UserCategoryService() {
        this(new UserCategoryRepositoryImpl());
    }

    public UserCategoryService(UserCategoryRepository userCategoryRepository) {
        super(userCategoryRepository);
        this.userCategoryRepository = userCategoryRepository;
    }
}