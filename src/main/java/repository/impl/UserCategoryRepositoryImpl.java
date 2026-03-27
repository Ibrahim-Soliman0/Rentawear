package repository.impl;

import entity.UserCategory;
import repository.UserCategoryRepository;

public class UserCategoryRepositoryImpl extends BaseRepositoryImpl<UserCategory> implements UserCategoryRepository {

    public UserCategoryRepositoryImpl() {
        super(UserCategory.class);
    }
}

