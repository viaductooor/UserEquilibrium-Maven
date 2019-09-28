package org.lab1505.ue.web.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.lab1505.ue.web.domain.User;

@Mapper
public interface UserDao {
    @Select({"SELECT * FROM user WHERE id = #{id}"})
    User getUserById(@Param("id")String id);
}
