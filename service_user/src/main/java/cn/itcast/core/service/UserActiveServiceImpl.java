package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UserActiveServiceImpl implements UserActiveService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserDao userDao;

    /**
     * 统计用户活跃度  储存到redis
     *
     * @param username
     */
    @Override
    public void addToRedis(String username) {
        LocalDate localDate = LocalDate.now();
        String date = String.valueOf(localDate);
        Integer count = (Integer) redisTemplate.boundHashOps(date).get("count");
        System.out.println(count);
        if (count == null) {
            count=1;
            redisTemplate.boundHashOps(date).put("count", count);
            redisTemplate.boundValueOps(date).expire(4, TimeUnit.DAYS);
        } else {
            count += 1;
            redisTemplate.boundHashOps(date).put("count", count);
        }

        //放入缓存
        // redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);
    }

}
