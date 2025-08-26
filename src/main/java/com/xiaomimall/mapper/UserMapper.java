package com.xiaomimall.mapper;

import com.xiaomimall.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    //  用户注册 insert(User user)
    @Insert("INSERT INTO users (username, password, email, phone, role) " +
            "VALUES (#{username}, #{password}, #{email}, #{phone}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    //  根据id查全部信息 findByUsername(String username)
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    //  根据名字查全部信息 findByUsername(String username)
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    //  根据邮箱查全部信息User findByEmail(String email);
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);

    //  根据手机号查全部 User findByPhone(String phone);
    @Select("SELECT * FROM users WHERE phone = #{phone}")
    User findByPhone(String phone);

    //  更新用户信息int updateUserInfo(User user);
    @Update("UPDATE users SET email = #{email}, phone = #{phone} WHERE id = #{id}")
    int updateUserInfo(User user);
}