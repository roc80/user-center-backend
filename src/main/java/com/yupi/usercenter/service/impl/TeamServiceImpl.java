package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.constant.TeamConstant;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.mapper.TeamMapper;
import com.yupi.usercenter.model.Team;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.base.ResponseUtils;
import com.yupi.usercenter.model.dto.TeamDTO;
import com.yupi.usercenter.model.dto.UserDTO;
import com.yupi.usercenter.model.enums.TeamTypeEnum;
import com.yupi.usercenter.model.request.TeamCreateRequest;
import com.yupi.usercenter.model.request.TeamUpdateRequest;
import com.yupi.usercenter.service.TeamService;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.utils.UserHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lipeng
 * @description 针对表【team】的数据库操作Service实现
 * @createDate 2025-05-23 10:09:11
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Autowired
    UserService userService;

    @Override
    public BaseResponse<Boolean> createTeam(TeamCreateRequest teamCreateRequest, @NotNull Long userId) {
        if (StringUtils.isBlank(teamCreateRequest.getName())) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "用户名不能为空字符串或空格");
        }
        TeamTypeEnum teamTypeEnum = getTeamTypeEnum(teamCreateRequest.getJoinType());
        if (teamTypeEnum == TeamTypeEnum.SECRET && teamCreateRequest.getJoinKey() == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "加密队伍必须要有密钥");
        }
        int maxNum = teamCreateRequest.getMaxNum();
        if (maxNum > TeamConstant.TEAM_MEMBER_NUM_MAX || maxNum <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "最大人数错误");
        }
//        查询已创建队伍数量，要<=5
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("owner_user_id", userId);
        long currentUserOwnedTeams = this.count(teamQueryWrapper);
        if (currentUserOwnedTeams >= 5) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "当前用户拥有的队伍数量超过5个，不允许继续创建!");
        }

        Team team = new Team(teamCreateRequest, userId);
        boolean saved = this.save(team);
        return ResponseUtils.success(saved);
    }

    @Override
    public BaseResponse<List<TeamDTO>> retrieveTeams(HttpServletRequest request) {
        boolean isAdmin = UserHelper.isAdmin(UserHelper.getUserDtoFromRequest(request));
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        if (!isAdmin) {
            teamQueryWrapper.ne("join_type", TeamTypeEnum.PRIVATE.getValue());
        }
        List<Team> list = this.list(teamQueryWrapper);
        List<TeamDTO> teamDTOList = getTeamDTOList(list);
        return ResponseUtils.success(teamDTOList);
    }

    @NotNull
    private List<TeamDTO> getTeamDTOList(List<Team> list) {
        // 查询当前队伍的用户信息
        return list.stream().map(team -> {
            List<UserDTO> userDTOList = new ArrayList<>();
            String memberIds = team.getMemberIds();
            ArrayList<String> idStrList = new ArrayList<>(Arrays.asList(memberIds.split(TeamConstant.TEAM_MEMBER_ID_SPLIT)));
            idStrList.forEach(idStr -> {
                long userId = Long.parseLong(idStr);
                BaseResponse<UserDTO> response = userService.searchUserByUserId(userId);
                if (response.getCode() != Error.OK.getCode()) {
                    throw new BusinessException(Error.SERVER_DIRTY_DATA, "");
                }
                userDTOList.add(response.getData());
            });
            return new TeamDTO(team, userDTOList);
        }).collect(Collectors.toList());
    }

    @Override
    public BaseResponse<List<TeamDTO>> retrieveTeamsByPage(HttpServletRequest request, int pageNum, int pageSize) {
        boolean isAdmin = UserHelper.isAdmin(UserHelper.getUserDtoFromRequest(request));
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        if (!isAdmin) {
            teamQueryWrapper.ne("join_type", TeamTypeEnum.PRIVATE.getValue());
        }
        Page<Team> page = this.page(new Page<>(pageNum, pageSize), teamQueryWrapper);
        List<Team> teamList = page.getRecords();
        return ResponseUtils.success(getTeamDTOList(teamList));
    }

    @Override
    public BaseResponse<Boolean> updateTeam(HttpServletRequest httpServletRequest, @NonNull TeamUpdateRequest teamUpdateRequest) {
        UserDTO loginUser = UserHelper.getUserDtoFromRequest(httpServletRequest);
        Team originalTeam = this.getById(teamUpdateRequest.getId());
        if (originalTeam == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "队伍不存在");
        }
        //        - [ ] 除管理员外，只有owner可以修改
        final boolean idAdmin = UserHelper.isAdmin(loginUser);
        if (!idAdmin) {
            if (originalTeam.getOwnerUserId() != Optional.ofNullable(loginUser.getUserId()).orElse(-1L)) {
                throw new BusinessException(Error.CLIENT_FORBIDDEN, "只有队长可以修改");
            }
        }
//        - [ ] name trim()之后 长度非0
        if (teamUpdateRequest.getName() != null && StringUtils.isBlank(teamUpdateRequest.getName())) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR ,"队伍名称不能为空");
        }
//        - [ ] maxNum [当前队员人数, max]
        List<String> memberIdList = Arrays.asList(originalTeam.getMemberIds().split(TeamConstant.TEAM_MEMBER_ID_SPLIT));
        int currentMemberNum = memberIdList.size();
        Integer updateRequestMaxNum = teamUpdateRequest.getMaxNum();
        if (updateRequestMaxNum != null) {
            if (updateRequestMaxNum < currentMemberNum || updateRequestMaxNum > TeamConstant.TEAM_MEMBER_NUM_MAX) {
                throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "队伍人数错误");
            }
        }
//        - [ ] ownerUsrId 必须在当前队伍
        Long newOwnerUserId = teamUpdateRequest.getOwnerUserId();
        if (newOwnerUserId != null && originalTeam.getOwnerUserId() != newOwnerUserId) {
            if (!memberIdList.contains(String.valueOf(newOwnerUserId))) {
                throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "队长必须在当前队伍中");
            }
        }
//        - [ ] joinType 改为加密时，必须携带 joinKey
        Integer joinType = teamUpdateRequest.getJoinType();
        if (joinType != null && getTeamTypeEnum(joinType) == TeamTypeEnum.SECRET) {
            if (StringUtils.isBlank(teamUpdateRequest.getJoinKey())) {
                throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "加密队伍必须设置密钥");
            }
        }
//        - [ ] status 只有管理员能更改
        if (teamUpdateRequest.getStatus() != null) {
            if (!idAdmin) {
                throw new BusinessException(Error.CLIENT_FORBIDDEN, "");
            }
        }
        BeanUtils.copyProperties(teamUpdateRequest, originalTeam);
        boolean isUpdate = this.updateById(originalTeam);
        return ResponseUtils.success(isUpdate);
    }

    @Override
    public BaseResponse<Boolean> deleteTeam(HttpServletRequest request, @NonNull Long teamId) {
        UserDTO loginUser = UserHelper.getUserDtoFromRequest(request);
        if (!UserHelper.isAdmin(loginUser)) {
            Team team = this.getById(teamId);
            if (team == null) {
                throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
            }
            long ownerUserId = team.getOwnerUserId();
            long loginUserId = Optional.ofNullable(loginUser.getUserId()).orElse(-1L);
            if (ownerUserId != loginUserId) {
                throw new BusinessException(Error.CLIENT_FORBIDDEN, "");
            }
        }
        boolean isDelete = this.removeById(teamId);
        return ResponseUtils.success(isDelete);
    }

    private TeamTypeEnum getTeamTypeEnum(int joinType) {
        TeamTypeEnum teamTypeEnum = TeamTypeEnum.Companion.fromValue(joinType);
        if (teamTypeEnum == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "加入方式错误");
        }
        return teamTypeEnum;
    }


}




