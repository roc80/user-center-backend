package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.algorithm.EditDistance;
import com.yupi.usercenter.config.MyConfigProperty;
import com.yupi.usercenter.constant.UserConstant;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.mapper.TagMapper;
import com.yupi.usercenter.mapper.UserMapper;
import com.yupi.usercenter.model.Tag;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.base.ResponseUtils;
import com.yupi.usercenter.model.dto.TagDTO;
import com.yupi.usercenter.model.dto.UserDTO;
import com.yupi.usercenter.model.helper.ModelHelper;
import com.yupi.usercenter.model.request.TagBindRequest;
import com.yupi.usercenter.model.request.UserLoginRequest;
import com.yupi.usercenter.model.response.LoginResponse;
import com.yupi.usercenter.model.response.PageResponse;
import com.yupi.usercenter.service.*;
import com.yupi.usercenter.utils.UserHelper;
import com.yupi.usercenter.utils.aspect.RequiredLogin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RedissonClient;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 针对表【user(用户表)】的数据库操作Service实现
 *
 * @author lipeng
 * @since 2024-10-21 18:42:42
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private static final int USERNAME_MIN_LENGTH = 4;
    private static final int USERNAME_MAX_LENGTH = 256;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 2048;

    private final CacheService cacheService;
    private final CacheKeyBuilder cacheKeyBuilder;
    private final UserTagService userTagService;
    private final MyConfigProperty myConfigProperty;
    private final TagMapper tagMapper;
    private final ImageUploadService imageUploadService;
    private final UserMapper userMapper;

    public UserServiceImpl(CacheService cacheService, CacheKeyBuilder cacheKeyBuilder, UserTagService userTagService, MyConfigProperty myConfigProperty, TagMapper tagMapper, ImageUploadService imageUploadService, RedissonClient redissonClient, UserMapper userMapper) {
        this.cacheService = cacheService;
        this.cacheKeyBuilder = cacheKeyBuilder;
        this.userTagService = userTagService;
        this.myConfigProperty = myConfigProperty;
        this.tagMapper = tagMapper;
        this.imageUploadService = imageUploadService;
        this.userMapper = userMapper;
    }

    public BaseResponse<Long> userRegister(@NonNull String userName, @NonNull String userPassword, @NonNull String repeatPassword) {
        if (StringUtils.isAnyBlank(userName, userPassword, repeatPassword)) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "用户名或密码不能为空");
        }
        String result = commonCheck(userName, userPassword);
        if (result != null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, result);
        }
        // 密码确认校验
        if (!userPassword.equals(repeatPassword)) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "两次输入的密码不一致");
        }
        // userName 唯一
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        long count = this.count(queryWrapper.eq("user_name", userName));
        if (count > 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "该用户名已存在");
        }
        // 插入一个新用户，返回用户id
        String userPasswordMd5 = DigestUtils.md5DigestAsHex((userPassword + myConfigProperty.getSecurity().getSalt()).getBytes());
        User newUser = new User(userName, userPasswordMd5);
        boolean saved = this.save(newUser);
        if (saved) {
            long totalUserNum = this.count();
            int changedPageNum = (int) (totalUserNum / UserConstant.USER_PAGE_SIZE + 1);
            updateSearchAllUserCache(changedPageNum);
            return ResponseUtils.success(newUser.getId());
        } else {
            throw new BusinessException(Error.SERVER_ERROR, "注册用户失败");
        }
    }

    private void updateSearchAllUserCache(int changedPageNum) {
        String userSearchKey = cacheKeyBuilder.buildUserSearchKey(changedPageNum, UserConstant.USER_PAGE_SIZE);
        boolean deleted = cacheService.deleteCache(userSearchKey);
        if (deleted) {
            log.info("delete cache {} success", userSearchKey);
        } else {
            log.error("delete cache {} failed", userSearchKey);
        }
    }

    private String commonCheck(String userName, String userPassword) {
        // 用户名长度校验
        if (userName.length() < USERNAME_MIN_LENGTH || userName.length() > USERNAME_MAX_LENGTH) {
            return "用户名长度异常";
        }
        // 用户名特殊字符校验
        String regex = "[!@#$%^&*()_+\\-={}\\[\\]:\";',.?/\\\\|]"; /*随便搜的regex */
        Matcher matcher = Pattern.compile(regex).matcher(userName);
        if (matcher.find()) {
            return "用户名不能包含特殊字符";
        }
        // 密码长度校验
        if (userPassword.length() < PASSWORD_MIN_LENGTH || userPassword.length() > PASSWORD_MAX_LENGTH) {
            return "密码长度异常";
        }
        // 如果全部校验通过，则必须返回Null
        return null;
    }

    public BaseResponse<LoginResponse> userLogin(UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        String userName = userLoginRequest.getUsername();
        String userPassword = userLoginRequest.getPassword();
        if (StringUtils.isAnyBlank(userName, userPassword)) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "用户名或密码不能为空");
        }
        String reason = commonCheck(userName, userPassword);
        if (reason != null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, reason);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName);
        User savedUser = this.getOne(queryWrapper);
        if (savedUser == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "该用户不存在");
        }
        // 密码校验
        String savedUserPasswordMD5 = savedUser.getUserPassword();
        String needCheckPasswordMD5 = DigestUtils.md5DigestAsHex((userPassword + myConfigProperty.getSecurity().getSalt()).getBytes());
        if (needCheckPasswordMD5.equals(savedUserPasswordMD5)) {
            UserDTO userDTO = ModelHelper.INSTANCE.convertUserToUserDto(savedUser);
            httpServletRequest.getSession().setAttribute(UserConstant.USER_LOGIN_INFO, userDTO);
            LoginResponse loginResponse = new LoginResponse(userDTO, userLoginRequest.getRedirectUrl());
            return ResponseUtils.success(loginResponse);
        } else {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "密码校验失败");
        }
    }

    @Override
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_INFO);
        return ResponseUtils.success(true);
    }

    @Override
    public BaseResponse<List<UserDTO>> searchUserByUserName(@NotNull String userName, HttpServletRequest request) {
        if (!UserHelper.isAdmin(UserHelper.getUserDtoFromRequest(request))) {
            throw new BusinessException(Error.CLIENT_FORBIDDEN, "无权限查询用户");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<User>().like("user_name", userName);
        List<User> originalUserList = this.list(userQueryWrapper);
        List<UserDTO> safetyUserList = originalUserList.stream().map(ModelHelper.INSTANCE::convertUserToUserDto).collect(Collectors.toList());
        return ResponseUtils.success(safetyUserList);
    }

    @Override
    @Transactional(timeout = 3, rollbackFor = Exception.class)
    public BaseResponse<Boolean> deleteUser(@NotNull Long userId, HttpServletRequest request) {
        if (!UserHelper.isAdmin(UserHelper.getUserDtoFromRequest(request))) {
            throw new BusinessException(Error.CLIENT_FORBIDDEN, "无权限删除用户");
        }
        boolean deleted = this.removeById(userId);
        if (!deleted) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "删除失败");
        }
        return ResponseUtils.success(true);
    }

    @Override
    public @Nullable BaseResponse<UserDTO> currentUser(HttpServletRequest request) {
        Object userLoginInfo = request.getSession().getAttribute(UserConstant.USER_LOGIN_INFO);
        if (!(userLoginInfo instanceof UserDTO)) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "session中存储的用户信息异常");
        }
        User userPO = this.getById(((UserDTO) (userLoginInfo)).getUserId());
        if (userPO == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "该用户不存在");
        }
        UserDTO userDTO = ModelHelper.INSTANCE.convertUserToUserDto(userPO);
        request.getSession().setAttribute(UserConstant.USER_LOGIN_INFO, userDTO);
        return ResponseUtils.success(userDTO);
    }

    @Override
    public BaseResponse<UserDTO> searchUserByUserId(@NotNull Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
        }
        return ResponseUtils.success(ModelHelper.INSTANCE.convertUserToUserDto(user));
    }

    @RequiredLogin
    @Override
    public BaseResponse<PageResponse<UserDTO>> searchAllUser(HttpServletRequest request, int pageNum, int pageSize) {
        validatePageSize(pageSize);
        PageResponse<UserDTO> pageResponse = cacheService.getWithCache(
                cacheKeyBuilder.buildUserSearchKey(pageNum, pageSize),
                () -> getUsersFromDB(pageNum, pageSize),
                Duration.ofHours(1)
        );
        return ResponseUtils.success(pageResponse);
    }

    private void validatePageSize(int pageSize) {
        if (pageSize != UserConstant.USER_PAGE_SIZE) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, String.format("允许的分页大小：%d", UserConstant.USER_PAGE_SIZE));
        }
    }

    public PageResponse<UserDTO> getUsersFromDB(int pageNum, int pageSize) {
        try {
            Page<User> resultPage = this.page(new Page<>(pageNum, pageSize));
            List<UserDTO> userDTOList = resultPage.getRecords().stream()
                    .map(ModelHelper.INSTANCE::convertUserToUserDto)
                    .collect(Collectors.toList());
            long counted = this.count();
            return new PageResponse<>(userDTOList, pageNum, pageSize, counted);
        } catch (Exception e) {
            log.error("Failed to query users from database, pageNum: {}, pageSize: {}", pageNum, pageSize, e);
            throw new BusinessException(Error.SERVER_ERROR, "分页查询用户失败， pageNum = " + pageNum + ", pageSize = " + pageSize);
        }
    }

    /**
     * @return 返回的用户包含传入的所有tag
     * @author lipeng
     * @since 2025/5/5 16:00
     */
    @Override
    public @NonNull BaseResponse<List<UserDTO>> searchUsersByTags(List<Long> tagIdList) {
        if (tagIdList == null || tagIdList.isEmpty()) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "tagIdList为空");
        }
        List<Long> filteredTagIdList = tagIdList.stream().filter(tagId -> tagMapper.selectById(tagId) != null).collect(Collectors.toList());
        List<User> matchedUserList = userTagService.getUserList(filteredTagIdList);
        List<UserDTO> users = matchedUserList.stream().map(ModelHelper.INSTANCE::convertUserToUserDto).collect(Collectors.toList());
        return ResponseUtils.success(users);
    }

    @Override
    public BaseResponse<Integer> updateUser(HttpServletRequest request, @Nullable UserDTO userDTO) {
        if (userDTO == null || userDTO.getUserId() == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_NULL, "");
        }
        // 校验权限
        UserDTO loginUser = UserHelper.getUserDtoFromRequest(request);
        boolean isSameUser = userDTO.getUserId().equals(loginUser.getUserId());
        boolean isAdmin = UserHelper.isAdmin(loginUser);
        if (!isAdmin && !isSameUser) {
            throw new BusinessException(Error.CLIENT_NO_AUTH, "无权限修改");
        }
        User partialUser = ModelHelper.INSTANCE.convertUserDtoToUser(userDTO);
        User userPo = this.getById(partialUser.getId());
        if (userPo == null || userPo.getId() == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "用户不存在");
        }
        updateUserProps(userPo, partialUser, isAdmin);
        boolean updated = this.updateById(userPo);
        if (updated) {
            long currentUserSerialNumber = userMapper.getSerialNumFromValidUsers(userPo.getId());
            int changedPageNum = (int) (currentUserSerialNumber / UserConstant.USER_PAGE_SIZE + 1);
            updateSearchAllUserCache(changedPageNum);
            return ResponseUtils.success(0);
        } else {
            return ResponseUtils.success(-1);
        }
    }

    /**
     * @param partialUser 前端更新用户信息后，传递的新用户对象。只有不为空的部分是需要写入数据库的。
     * @author lipeng
     * @since 2025/5/15 11:03
     */
    private void updateUserProps(User originalUser, User partialUser, boolean isAdmin) {
        if (originalUser == null || partialUser == null) {
            return;
        }
        if (partialUser.getUserName() != null) {
            originalUser.setUserName(partialUser.getUserName());
        }
        if (partialUser.getGender() != null) {
            originalUser.setGender(partialUser.getGender());
        }
        if (partialUser.getEmail() != null) {
            originalUser.setEmail(partialUser.getEmail());
        }
        if (partialUser.getPhone() != null) {
            originalUser.setPhone(partialUser.getPhone());
        }
        if (isAdmin) {
            if (partialUser.isValid() != null) {
                originalUser.setValid(partialUser.isValid());
            }
            if (partialUser.getUserRole() != null) {
                originalUser.setUserRole(partialUser.getUserRole());
            }
        }
    }

    @Override
    public BaseResponse<PageResponse<UserDTO>> recommendUsers(HttpServletRequest request, int pageNum, int pageSize) {
        UserDTO loginUser = UserHelper.getUserDtoFromRequest(request);
        User userPo = this.getById(loginUser.getUserId());
        if (userPo == null) {
            throw new BusinessException(Error.CLIENT_NO_AUTH, "");
        }
        validatePageSize(pageSize);
        // 用户无标签时，推荐用户 降级为 展示所有用户。
        if (userTagService.getTagList(userPo.getId()).isEmpty()) {
            return searchAllUser(request, pageNum, pageSize);
        }
        PageResponse<UserDTO> pageResponse = cacheService.getWithCache(
                cacheKeyBuilder.buildUserRecommendKey(userPo.getId(), pageNum, pageSize),
                () -> recommendUsersFromDB(userPo, pageNum, pageSize),
                Duration.ofMinutes(1)
        );
        return ResponseUtils.success(pageResponse);
    }

    public PageResponse<UserDTO> recommendUsersFromDB(User sourceUser, int pageNum, int pageSize) {
        List<Long> hasTagUserIdList = userTagService.getAllUserWithTag();
        List<String> sourceTagList = userTagService.getTagNameList(sourceUser.getId());

        PriorityQueue<Long> maximumPriorityQueue = new PriorityQueue<>(pageNum * pageSize, (userId1, userId2) -> {
            List<String> user1TagList = userTagService.getTagNameList(userId1);
            List<String> user2TagList = userTagService.getTagNameList(userId2);
            int user1Distance = EditDistance.levenshteinDistance(sourceTagList, user1TagList);
            int user2Distance = EditDistance.levenshteinDistance(sourceTagList, user2TagList);
            // PriorityQueue默认是最小堆，这里需要得到前pageSize个编辑距离最小的元素，所以用最大堆。
            return user2Distance - user1Distance;
        });
        long totalRecommendUserNum = 0L;
        for (Long userId : hasTagUserIdList) {
            Long sId = sourceUser.getId();
            if (Objects.equals(userId, sId)) {
                continue;
            }
            totalRecommendUserNum ++;
            maximumPriorityQueue.offer(userId);
        }
        while (maximumPriorityQueue.size() > pageSize) {
            maximumPriorityQueue.poll();
        }
        LinkedList<User> strictlySortedUserList = new LinkedList<>();
        while (!maximumPriorityQueue.isEmpty()) {
            strictlySortedUserList.addFirst(this.getById(maximumPriorityQueue.poll()));
        }
        List<UserDTO> userDTOList = strictlySortedUserList.stream().map(ModelHelper.INSTANCE::convertUserToUserDto).collect(Collectors.toList());
        return new PageResponse<>(userDTOList, pageNum, pageSize, totalRecommendUserNum);
    }

    @Override
    public BaseResponse<List<TagDTO>> getUserTags(Long userId) {
        List<Tag> tagList = userTagService.getTagList(userId);
        List<TagDTO> tagDTOList = tagList.stream().filter(tag -> tag.isParent() == 0).map(TagDTO::new).collect(Collectors.toList());
        return ResponseUtils.success(tagDTOList);
    }

    @Override
    public BaseResponse<Integer> updateTags(HttpServletRequest request, TagBindRequest tagBindRequest) {
        User loginUser = this.getById(UserHelper.getUserDtoFromRequest(request).getUserId());
        if (loginUser == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "用户不存在");
        }
        List<Long> tagIdList = tagBindRequest.getTagIdList();
        List<Long> validTagIdList = tagIdList.stream().filter(tagId -> tagMapper.selectById(tagId) != null).collect(Collectors.toList());
        Integer addedTagNum = userTagService.updateTagsOnUser(loginUser.getId(), validTagIdList);
        if (addedTagNum == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
        }
        return ResponseUtils.success(addedTagNum);
    }

    @Override
    public BaseResponse<String> uploadAvatar(MultipartFile file, Long userId, HttpServletRequest request) {
        UserDTO loginUser = UserHelper.getUserDtoFromRequest(request);
        User user = this.getById(loginUser.getUserId());
        if (user == null || !Objects.equals(user.getId(), userId)) {
            throw new BusinessException(Error.CLIENT_FORBIDDEN, "");
        }
        String avatarUrl = imageUploadService.uploadAvatar(file, userId);
        if (avatarUrl != null) {
            log.info("userId:{} upload avatar success, avatarUrl = {}", userId, avatarUrl);
            user.setAvatarUrl(avatarUrl);
            boolean updated = this.updateById(user);
            if (updated) {
                log.info("userId:{} update avatarUrl:{} success.", userId, avatarUrl);
            }
            return ResponseUtils.success(avatarUrl);
        } else {
            log.error("userId:{} upload avatar failed.", userId);
            return ResponseUtils.error(Error.SERVER_ERROR, "上传头像失败");
        }
    }

}