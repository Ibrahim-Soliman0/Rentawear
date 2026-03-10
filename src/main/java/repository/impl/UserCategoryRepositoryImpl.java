package repository.impl;

import model.UserCategory;
import repository.UserCategoryRepository;

public class UserCategoryRepositoryImpl extends BaseRepositoryImpl<UserCategory> implements UserCategoryRepository {

    public UserCategoryRepositoryImpl() {
        super(UserCategory.class);
    }
}

