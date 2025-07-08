package org.progingo.service;

import org.progingo.model.User;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 获取用户
     *
     * @param user
     * @return
     */
    User getUser(User user);

    /**
     * 新方法 - 获取数字。用于测试是否走了mock
     */
    default short getNumber() {
        return 1;
    }
}
