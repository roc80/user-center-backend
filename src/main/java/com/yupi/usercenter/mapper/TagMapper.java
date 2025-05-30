package com.yupi.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.usercenter.model.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lipeng
* @description 针对表【tag(标签表)】的数据库操作Mapper
* @createDate 2025-05-05 15:26:29
* @Entity com.yupi.usercenter.model.Tag
*/
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

}




