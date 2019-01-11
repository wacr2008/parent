package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.user.User;

public interface UserService {
    public void sendCode(String phone);

    public Boolean checkSmsCode(String phone , String smsCode);

    public  void  add(User user);


    PageResult findPage(User user, Integer page, Integer rows);

    void updateStatus(Long id, String status);

    String check(String username);

}
