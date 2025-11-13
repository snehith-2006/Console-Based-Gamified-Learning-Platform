package com.algoarena.dao;

import com.algoarena.models.Person;
import com.algoarena.models.User;

public interface IUserDAO {

    Person login(String username, String password);
    
    boolean register(User user);
}