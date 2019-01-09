package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    /**
     * 这个是队列目的地，点对点的  文本信息
     */
    @Autowired
    private ActiveMQQueue smsDestination;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserDao userDao;

    @Override
    public void sendCode(String phone) {
        String six = getSix();
      // System.out.println(six);
        redisTemplate.boundValueOps(phone).set(six, 5, TimeUnit.MINUTES);

        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Map map = new HashMap<>();
              //  map.put(, );

                return null;
            }
        });

    }

    @Override
    public Boolean checkSmsCode(String phone, String smsCode) {
        if (phone != null && !"".equals(phone) && smsCode != null && !"".equals(smsCode)) {
           return  false;
        }
        String o = (String) redisTemplate.boundValueOps(phone).get();
        //3. 根据页面传入的验证码和我们自己存的验证码对比是否正确
        if (smsCode.equals(o)) {
            return true;
        }
        return false;
    }

    @Override
    public void add(User user) {
        userDao.insertSelective(user);

    }

    /**
     * 用户分页查询
     * @return
     */
    @Override
    public PageResult search() {
        return null;
    }

    /**
     * 获取一个6位的随机数
     *
     * @return
     */
    private static String getSix() {

        Random random = new Random();
        String result = random.nextInt(1000000) + "";
        if (result.length() != 6) {
            return getSix();
        }
        return result;
    }
}
