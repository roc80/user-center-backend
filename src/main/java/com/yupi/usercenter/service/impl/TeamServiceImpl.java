package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.constant.TeamConstant;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.mapper.TeamMapper;
import com.yupi.usercenter.model.Team;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.base.ResponseUtils;
import com.yupi.usercenter.model.dto.TeamDTO;
import com.yupi.usercenter.model.dto.UserDTO;
import com.yupi.usercenter.model.enums.TeamTypeEnum;
import com.yupi.usercenter.model.request.TeamCreateRequest;
import com.yupi.usercenter.model.request.TeamUpdateRequest;
import com.yupi.usercenter.model.request.UserExitTeamRequest;
import com.yupi.usercenter.model.request.UserJoinTeamRequest;
import com.yupi.usercenter.service.TeamService;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.utils.BeanUtilsExtend;
import com.yupi.usercenter.utils.UserHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
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

    @Autowired
    TeamMapper teamMapper;

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
        int currentUserOwnedTeams = teamMapper.selectTeamsByOwnerUserId(userId);
        if (currentUserOwnedTeams >= 5) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "当前用户拥有的队伍数量超过5个，不允许继续创建!");
        }

        Team team = new Team(teamCreateRequest, userId);
        boolean saved = this.save(team);
        return ResponseUtils.success(saved);
    }

    @Override
    public BaseResponse<List<TeamDTO>> retrieveTeams(HttpServletRequest request) {
        boolean queryAllJoinType = UserHelper.isAdmin(UserHelper.getUserDtoFromRequest(request));
        List<Team> list = teamMapper.selectAllTeam(queryAllJoinType, TeamTypeEnum.PRIVATE.getValue());
        List<TeamDTO> teamDTOList = getTeamDTOList(list);
        return ResponseUtils.success(teamDTOList);
    }

    @NotNull
    private List<TeamDTO> getTeamDTOList(List<Team> list) {
        // 查询当前队伍的用户信息
        return list.stream().map(team -> {
            List<UserDTO> userDTOList = new ArrayList<>();
            List<Long> memberIdList = convertMemberIdsToList(team.getMemberIds());
            memberIdList.forEach(userId -> {
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
        if (pageNum <= 0 || pageSize <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
        }
        boolean queryAllJoinType = UserHelper.isAdmin(UserHelper.getUserDtoFromRequest(request));
        List<Team> teamList = teamMapper.selectTeamByPage(
                queryAllJoinType,
                TeamTypeEnum.PRIVATE.getValue(),
                (pageNum - 1) * pageSize,
                pageSize
        );
        return ResponseUtils.success(getTeamDTOList(teamList));
    }

    @Override
    public BaseResponse<Boolean> updateTeam(HttpServletRequest httpServletRequest, @NonNull TeamUpdateRequest teamUpdateRequest) {
        UserDTO loginUser = UserHelper.getUserDtoFromRequest(httpServletRequest);
        Team teamPo = this.getById(teamUpdateRequest.getId());
        if (teamPo == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "队伍不存在");
        }
        //        - [ ] 除管理员外，只有owner可以修改
        final boolean idAdmin = UserHelper.isAdmin(loginUser);
        if (!idAdmin) {
            if (teamPo.getOwnerUserId() != Optional.ofNullable(loginUser.getUserId()).orElse(-1L)) {
                throw new BusinessException(Error.CLIENT_FORBIDDEN, "只有队长可以修改");
            }
        }
//        - [ ] name trim()之后 长度非0
        if (teamUpdateRequest.getName() != null && StringUtils.isBlank(teamUpdateRequest.getName())) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR ,"队伍名称不能为空");
        }
//        - [ ] maxNum [当前队员人数, max]
        List<Long> memberIdList = convertMemberIdsToList(teamPo.getMemberIds());
        int currentMemberNum = memberIdList.size();
        Integer updateRequestMaxNum = teamUpdateRequest.getMaxNum();
        if (updateRequestMaxNum != null) {
            if (updateRequestMaxNum < currentMemberNum || updateRequestMaxNum > TeamConstant.TEAM_MEMBER_NUM_MAX) {
                throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "队伍人数错误");
            }
        }
//        - [ ] ownerUsrId 必须在当前队伍
        Long newOwnerUserId = teamUpdateRequest.getOwnerUserId();
        if (newOwnerUserId != null && teamPo.getOwnerUserId() != newOwnerUserId) {
            if (!memberIdList.contains(newOwnerUserId)) {
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
        String[] nullPropertyNames = BeanUtilsExtend.getNullPropertyNames(teamUpdateRequest);
        BeanUtils.copyProperties(teamUpdateRequest, teamPo, nullPropertyNames);
        boolean isUpdate = this.updateById(teamPo);
        return ResponseUtils.success(isUpdate);
    }

    @NotNull
    private static List<Long> convertMemberIdsToList(@NonNull String memberIds) {
        return Arrays.stream(memberIds.split(TeamConstant.TEAM_MEMBER_ID_SPLIT))
                .map(Long::parseLong).collect(Collectors.toList());
    }

    private static String convertListToMemberIds(List<Long> ids) {
        StringBuilder stringBuilder = new StringBuilder();
        ids.forEach(id -> stringBuilder.append(id).append(TeamConstant.TEAM_MEMBER_ID_SPLIT));
        return stringBuilder.toString();
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

    @Override
    public BaseResponse<Boolean> userJoinTeam(HttpServletRequest request, @NotNull Long teamId, UserJoinTeamRequest userJoinTeamRequest) {
        UserDTO loginUserDto = UserHelper.getUserDtoFromRequest(request);
        User userPo = userService.getById(loginUserDto.getUserId());
        if (userPo == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "用户信息错误");
        }
        //- [ ] 将加入的userId的状态正常
        if (!UserHelper.isUsrValid(loginUserDto)) {
            throw new BusinessException(Error.CLIENT_OPERATION_DENIED, "用户状态异常");
        }
        Team teamPo = this.getById(teamId);
        if (teamPo == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "队伍不存在");
        }
        //- [ ] 校验队伍加入方式--不可加入直接拒绝、密码加入则校验密码。
        TeamTypeEnum typeEnum = TeamTypeEnum.Companion.fromValue(teamPo.getJoinType());
        if (typeEnum == TeamTypeEnum.PRIVATE) {
            throw new BusinessException(Error.CLIENT_OPERATION_DENIED, "该队伍不可加入");
        } else if (typeEnum == TeamTypeEnum.SECRET) {
            if (teamPo.getJoinKey() == null) {
                throw new BusinessException(Error.SERVER_DIRTY_DATA, "");
            } else {
                if (!teamPo.getJoinKey().equals(userJoinTeamRequest.getJoinKey())) {
                    throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "入队密码错误");
                }
            }
        }
        //- [ ] 校验自己是否已经在队伍中（幂等）
        List<Long> memberIdList = convertMemberIdsToList(teamPo.getMemberIds());
        if (memberIdList.contains(userPo.getId())) {
            throw new BusinessException(Error.CLIENT_OPERATION_DENIED, "当前用户已加入该队伍");
        }
        // - [ ] 当前队伍的成员数量 < maxNum
        int curNum = memberIdList.size();
        if (curNum >= teamPo.getMaxNum()) {
            throw new BusinessException(Error.CLIENT_OPERATION_DENIED, "当前队伍已满员");
        }
        List<Long> idList = convertMemberIdsToList(teamPo.getMemberIds());
        idList.add(userPo.getId());
        String newMemberIds = convertListToMemberIds(idList);
        int rows = teamMapper.updateTeamMemberIds(teamPo.getId(), newMemberIds);
        boolean result = rows > 0;
        return ResponseUtils.success(result);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 3, rollbackFor = Exception.class)
    public BaseResponse<Boolean> userExitTeam(HttpServletRequest request, Long teamId, Long exitUserId, UserExitTeamRequest userExitTeamRequest) {
        if (teamId == null || teamId <= 0 || exitUserId == null || exitUserId <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
        }
        UserDTO loginUserDto = UserHelper.getUserDtoFromRequest(request);
        User loginUserPo = userService.getById(loginUserDto.getUserId());
        if (loginUserPo == null) {
            throw new BusinessException(Error.CLIENT_NO_AUTH, "");
        }
        Team teamPo = this.getById(teamId);
        if (teamPo == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
        }
        // 已登录用户自己能退，队长能退出本队队员，管理员能退出任意队伍的任意队员。
        boolean curIsAdmin = UserHelper.isAdmin(loginUserDto);
        boolean curIsOwner = Objects.equals(loginUserPo.getId(), teamPo.getOwnerUserId());
        boolean isSelfExit = Objects.equals(loginUserPo.getId(), exitUserId);
        if (!curIsAdmin && !curIsOwner && !isSelfExit) {
            throw new BusinessException(Error.CLIENT_FORBIDDEN, "");
        }
        //- [ ] 校验userId是否还在teamId之中
        List<Long> memberIdList = convertMemberIdsToList(teamPo.getMemberIds());
        if (!memberIdList.contains(exitUserId)) {
            throw new BusinessException(Error.CLIENT_PATH_ERROR, "");
        }

        boolean result;
        if (exitUserId == teamPo.getOwnerUserId()) {
            // 交接队长 or delete team
            if (memberIdList.size() > 1) {
                if (userExitTeamRequest == null || !memberIdList.contains(userExitTeamRequest.getNextOwnerUserId())) {
                    throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "新任队长不合法");
                }
                int rows1 = teamMapper.updateTeamOwner(teamPo.getId(), userExitTeamRequest.getNextOwnerUserId());
                int rows2 = deleteMember(exitUserId, teamPo.getId(), memberIdList);
                result = rows1 > 0 && rows2 > 0;
            } else {
                result = this.removeById(teamPo.getId());
            }
        } else {
            int rows = deleteMember(exitUserId, teamPo.getId(), memberIdList);
            result = rows > 0;
        }
        return ResponseUtils.success(result);
    }

    private int deleteMember(Long exitUserId, Long teamId, List<Long> memberIdList) {
        memberIdList.remove(exitUserId);
        String newMemberIds = convertListToMemberIds(memberIdList);
        return teamMapper.updateTeamMemberIds(teamId, newMemberIds);
    }

    private TeamTypeEnum getTeamTypeEnum(int joinType) {
        TeamTypeEnum teamTypeEnum = TeamTypeEnum.Companion.fromValue(joinType);
        if (teamTypeEnum == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "加入方式错误");
        }
        return teamTypeEnum;
    }


}




