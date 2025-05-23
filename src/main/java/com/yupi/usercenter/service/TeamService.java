package com.yupi.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenter.model.Team;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.request.TeamCreateRequest;
import org.springframework.lang.NonNull;

/**
* @author lipeng
* @description 针对表【team】的数据库操作Service
* @createDate 2025-05-23 10:09:11
*/
public interface TeamService extends IService<Team> {

    BaseResponse<Boolean> createTeam(TeamCreateRequest request, @NonNull Long userId);
}
