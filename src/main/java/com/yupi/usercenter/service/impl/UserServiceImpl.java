package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.yupi.usercenter.algorithm.EditDistance;
import com.yupi.usercenter.constant.RedisConstant;
import com.yupi.usercenter.constant.UserConstant;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.mapper.UserMapper;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.base.ResponseUtils;
import com.yupi.usercenter.model.dto.UserDTO;
import com.yupi.usercenter.model.helper.ModelHelper;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.utils.UserHelper;
import com.yupi.usercenter.utils.aspect.RequiredLogin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.ZoneId;
import java.util.*;
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

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    UserMapper userMapper;

    private static final int USERNAME_MIN_LENGTH = 4;
    private static final int USERNAME_MAX_LENGTH = 256;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 2048;

    // TODO@lp 不能硬编码在类中
    public static final String SUFFIX_SALT = "suARnTClqnWOx8";

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
        String userPasswordMd5 = DigestUtils.md5DigestAsHex((userPassword + SUFFIX_SALT).getBytes());
        User newUser = new User(userName, userPasswordMd5);
        this.save(newUser);
        return ResponseUtils.success(newUser.getId());
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

    public BaseResponse<UserDTO> userLogin(@NonNull String userName, @NonNull String userPassword, HttpServletRequest request) {
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
        String needCheckPasswordMD5 = DigestUtils.md5DigestAsHex((userPassword + SUFFIX_SALT).getBytes());
        if (needCheckPasswordMD5.equals(savedUserPasswordMD5)) {
            UserDTO userDTO = ModelHelper.INSTANCE.convertUserToUserDto(savedUser);
            request.getSession().setAttribute(UserConstant.USER_LOGIN_INFO, userDTO);
            return ResponseUtils.success(userDTO);
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
        if (user != null) {
            return ResponseUtils.success(ModelHelper.INSTANCE.convertUserToUserDto(user));
        } else {
            return ResponseUtils.error(Error.CLIENT_PARAMS_ERROR);
        }
    }

    @RequiredLogin
    @Override
    public BaseResponse<List<UserDTO>> searchAllUser(HttpServletRequest request, int pageNum, int pageSize) {
        Page<User> resultPage = null;
        // 取缓存
        String redisKeyName = String.format("%s:%s:pageNum=%d,pageSize=%d",
                RedisConstant.PROJECT_NAME,
                "all-user",
                pageNum,
                pageSize
        );
        RBucket<Page<User>> rBucket = redissonClient.getBucket(redisKeyName);
        try {
            resultPage = rBucket.get();
            if (resultPage == null) {
                log.info("key={},searchAllUser hit cache failed", redisKeyName);
                // 没取到，查库
                resultPage = this.page(new Page<>(pageNum, pageSize));
                // 写缓存 TODO@lp 过期时间加offset,防止缓存雪崩。
                rBucket.set(resultPage, Duration.ofHours(8));
            } else {
                log.info("key={}, searchAllUser hit cache succeed", redisKeyName);
            }
        } catch (RuntimeException e) {
            log.error("key={}, searchAllUser write redis error", redisKeyName, e);
        }
        // 没走缓存
        if (resultPage == null) {
            resultPage = this.page(new Page<>(pageNum, pageSize));
        }
        List<UserDTO> safetyUserList = resultPage.getRecords().stream().map(ModelHelper.INSTANCE::convertUserToUserDto).collect(Collectors.toList());
        return ResponseUtils.success(safetyUserList);
    }

    /**
     * @return 返回的用户包含传入的所有tag
     * @author lipeng
     * @since 2025/5/5 16:00
     */
    @Override
    public @NonNull BaseResponse<Set<UserDTO>> searchUsersByTags(@Nullable List<String> tagNameList) {
        if (tagNameList == null || tagNameList.isEmpty()) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "tags为空");
        }
        List<User> matchedUserList = searchUsersInDB(tagNameList);
        Set<UserDTO> users = matchedUserList.stream().map(ModelHelper.INSTANCE::convertUserToUserDto).collect(Collectors.toSet());
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
        if (!UserHelper.isAdmin(loginUser) && !isSameUser) {
            throw new BusinessException(Error.CLIENT_NO_AUTH, "无权限修改");
        }
        User partialUser = ModelHelper.INSTANCE.convertUserDtoToUser(userDTO);
        User oldUser = this.getById(partialUser.getId());
        updateUser(oldUser, partialUser);
        boolean updated = this.updateById(oldUser);
        if (updated) {
            return ResponseUtils.success(0);
        } else {
            return ResponseUtils.success(-1);
        }
    }

    @Override
    public BaseResponse<List<UserDTO>> recommendUsers(HttpServletRequest request, int pageNum, int pageSize) {
        UserDTO loginUser = UserHelper.getUserDtoFromRequest(request);
        User userPo = this.getById(loginUser.getUserId());
        if (userPo == null) {
            throw new BusinessException(Error.CLIENT_NO_AUTH, "");
        }
        if (userPo.getTagJsonList() == null) {
            return searchAllUser(request, pageNum, pageSize);
        }
        Page<User> resultPage = null;
        // TODO@lp 哪些用户使用缓存
        Long userId = userPo.getId();
        if (userId != null && userId < 100) {
            // 取缓存
            String redisKeyName = String.format(
                    "%s:%s:userId=%d:pageNum=%d,pageSize=%d",
                    RedisConstant.PROJECT_NAME,
                    RedisConstant.MODULE_RECOMMEND,
                    userId,
                    pageNum,
                    pageSize
            );
            RBucket<Page<User>> rBucket = redissonClient.getBucket(redisKeyName);
            try {
                resultPage = rBucket.get();
                if (resultPage == null) {
                    log.info("key={}, recommendUsers hit cache failed", redisKeyName);
                    // 没取到，查库
                    resultPage = recommendUsersFromDB(userPo, pageNum, pageSize);
                    // 写缓存 TODO@lp 过期时间加offset,防止缓存雪崩。
                    rBucket.set(resultPage, Duration.ofHours(8));
                } else {
                    log.info("key={}, recommendUsers hit cache succeed", redisKeyName);
                }
            } catch (RuntimeException e) {
                log.error("key={}, recommendUsers write redis error", redisKeyName, e);
            }
        }
        // 没走缓存
        if (resultPage == null) {
            resultPage = recommendUsersFromDB(userPo, pageNum, pageSize);
        }
        List<UserDTO> safetyUserList = resultPage.getRecords().stream().map(ModelHelper.INSTANCE::convertUserToUserDto).collect(Collectors.toList());
        return ResponseUtils.success(safetyUserList);
    }

    private Page<User> recommendUsersFromDB(User sourceUser, int pageNum, int pageSize) {
        List<User> hasTagsUserList = userMapper.selectUsersHasTags();
        List<String> sourceTagList = convertTagJsonToList(sourceUser.getTagJsonList());
        PriorityQueue<User> maximumPriorityQueue = new PriorityQueue<>(pageSize, (user1, user2) -> {
            List<String> user1TagList = convertTagJsonToList(user1.getTagJsonList());
            List<String> user2TagList = convertTagJsonToList(user2.getTagJsonList());
            int user1Distance = EditDistance.levenshteinDistance(sourceTagList, user1TagList);
            int user2Distance = EditDistance.levenshteinDistance(sourceTagList, user2TagList);
            // PriorityQueue默认是最小堆，这里需要得到前pageSize个编辑距离最小的元素，所以用最大堆。
            return user2Distance - user1Distance;
        });
        for (User user : hasTagsUserList) {
            if (Objects.equals(user.getId(), sourceUser.getId())) {
                continue;
            }
            maximumPriorityQueue.offer(user);
        }
        LinkedList<User> strictlySortedUserList = new LinkedList<>();
        while (!maximumPriorityQueue.isEmpty()) {
            strictlySortedUserList.addFirst(maximumPriorityQueue.poll());
        }
        // userMapper.selectUsersHasTags 为了提升查询性能，并没有select *
        List<User> resultUserList = strictlySortedUserList.stream().map(user -> this.getById(user.getId())).collect(Collectors.toList());
        return new Page<User>(pageNum, pageSize).setRecords(resultUserList);
    }

    private List<String> convertTagJsonToList(@Nullable String tagJson) {
        String nonNullTagJson = Optional.ofNullable(tagJson).orElse("");
        List<String> result = new ArrayList<>();
        try {
            result = new Gson().fromJson(nonNullTagJson, new TypeToken<List<String>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            log.error("parse tag json error", e);
        }
        return result;
    }

    /**
     * @param partialUser 前端更新用户信息后，传递的新用户对象。只有不为空的部分是需要写入数据库的。
     * @author lipeng
     * @since 2025/5/15 11:03
     */
    private void updateUser(User originalUser, User partialUser) {
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
        if (partialUser.getEmail() != null) {
            originalUser.setEmail(partialUser.getEmail());
        }
        if (partialUser.getEmail() != null) {
            originalUser.setEmail(partialUser.getEmail());
        }
        if (partialUser.getTagJsonList() != null) {
            originalUser.setTagJsonList(partialUser.getTagJsonList());
        }
        if (partialUser.isValid() != null) {
            originalUser.setValid(partialUser.isValid());
        }
        if (partialUser.getUserRole() != null) {
            originalUser.setUserRole(partialUser.getUserRole());
        }
        originalUser.setUpdateDatetime(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    /**
     * 适合用户数量较少时，可以和和数据库查询搭配着查
     *
     * @author lipeng
     * @since 2025/5/20 12:51
     */
    private @NonNull List<User> searchUsersInMem(@NonNull List<String> tagNameList) {
        List<User> userList = this.list();
        return userList.stream().filter(user -> {
            String tagJsonStr = user.getTagJsonList();
            if (tagJsonStr == null) {
                return false;
            }
            Type type = new TypeToken<Set<String>>() {
            }.getType();
            Set<String> userTagNameSet = new Gson().fromJson(tagJsonStr, type);
            for (String inputTagName : tagNameList) {
                if (!userTagNameSet.contains(inputTagName)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    private @NonNull List<User> searchUsersInDB(@NonNull List<String> tagNameList) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tag_json_list", tagName);
        }
        return this.list(queryWrapper);
    }

}