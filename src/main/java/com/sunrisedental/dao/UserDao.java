package com.sunrisedental.dao;

import com.sunrisedental.model.User;

import java.sql.SQLException;
import java.util.Optional;

public interface UserDao {

    Optional<User> findByUsername(String username) throws SQLException;
}