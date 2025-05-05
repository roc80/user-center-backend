package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.mapper.TagMapper;
import com.yupi.usercenter.model.Tag;
import com.yupi.usercenter.service.TagService;
import org.springframework.stereotype.Service;

/**
* @author lipeng
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2025-05-05 15:26:29
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




