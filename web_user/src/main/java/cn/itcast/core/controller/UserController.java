package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;

    @RequestMapping("/sendCode")
    public Result sendCode(String phone) {
        try {
            System.out.println();
            if (phone == null || "".equals(phone) || phone.length() != 11) {
                return new Result(false, "请输入正确的手机号!!!");
            }
            userService.sendCode(phone);
            return new Result(true, "发送成功!!!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "发送失败!!!");
        }
    }
    @RequestMapping("/add")
     public Result add(String smscode,@RequestBody User user){
         try {
             Boolean b = userService.checkSmsCode(user.getPhone(), smscode);
             if (!b){
                 return new Result(false, "手机号或者验证码不正确!");
             }
             user.setCreated(new Date());
             user.setUpdated(new Date());
             user.setSourceType("1");
             user.setStatus("Y");
             userService.add(user);
             return new Result(true, "注册成功!!!");
         } catch (Exception e) {
             e.printStackTrace();
             return new Result(false, "注册失败!!!");
         }

     }

}
