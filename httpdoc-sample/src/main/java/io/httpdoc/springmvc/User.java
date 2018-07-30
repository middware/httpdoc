package io.httpdoc.springmvc;

import io.httpdoc.core.annotation.Alias;

import java.util.List;

/**
 * 用户
 * @author 杨昌沛 646742615@qq.com
 * @date 2018-06-01 14:18
 **/
public class User {
    private String id;
    private String name;
    private List<List<User>> friends;

    public User() {
    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Alias("$id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<List<User>> getFriends() {
        return friends;
    }

    public void setFriends(List<List<User>> friends) {
        this.friends = friends;
    }
}
