package com.yupi.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.usercenter.model.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【user(用户表)】的数据库操作Mapper
 *
 * @author lipeng
 * @since 2024-10-21 18:42:42
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




