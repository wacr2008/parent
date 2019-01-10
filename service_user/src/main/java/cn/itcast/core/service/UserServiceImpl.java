package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.pojo.user.UserQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.sound.midi.Soundbank;
import javax.xml.stream.XMLOutputFactory;
import java.util.HashMap;
import java.util.List;
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
            return false;
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
     *
     * @return
     */
    @Override
    public PageResult findPage(User user, Integer pageNum, Integer pageSize) {
        System.out.println(user);
        PageHelper.startPage(pageNum, pageSize);
        UserQuery userQuery = new UserQuery();
        UserQuery.Criteria criteria = userQuery.createCriteria();

        if (user != null) {
            if (user.getUsername() != null && user.getUsername().length() > 0) {
                criteria.andUsernameLike("%" + user.getUsername() + "%");
            }
            if (user.getStatus() != null && user.getStatus().length() > 0 && user.getStatus() != "") {

                criteria.andStatusEqualTo(user.getStatus());
            }
        }
        Page<User> users = (Page<User>) userDao.selectByExample(userQuery);
        System.out.println(users.getResult());
        return new PageResult(users.getTotal(), users.getResult());
    }

    /**
     * @param id     用户id
     * @param status 状态
     *               用户状态修改
     */

    @Override
    public void updateStatus(Long id, String status) {

        //1. 修改用户状态
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userDao.updateByPrimaryKeySelective(user);
    }

    @Override
    public Boolean check(String username) {
        UserQuery userQuery=new UserQuery();
        UserQuery.Criteria criteria = userQuery.createCriteria();
        criteria.andUsernameEqualTo(username);
        List<User> users = userDao.selectByExample(userQuery);
        for (User user : users) {
            if ("N".equals(user.getStatus())){
                return false;
            }
        }
        return true;
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
