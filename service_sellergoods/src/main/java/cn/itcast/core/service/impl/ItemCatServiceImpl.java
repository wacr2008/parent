package cn.itcast.core.service.impl;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.service.ItemCatService;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private ItemCatDao itemCatDao;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public List<ItemCat> findByParentId(Long parentId) {
        List<ItemCat> itemCats = itemCatDao.selectByExample(null);
        for (ItemCat itemCat : itemCats) {
            redisTemplate.boundHashOps(Constants.CATEGORY_LIST_REDIS).put(itemCat.getName(), itemCat.getTypeId());
        }


        ItemCatQuery itemCatQuery = new ItemCatQuery();
        ItemCatQuery.Criteria criteria = itemCatQuery.createCriteria();
        criteria.andParentIdEqualTo(parentId);

        return itemCatDao.selectByExample(itemCatQuery);

    }

    @Override
    public ItemCat findOne(Long id) {

        return itemCatDao.selectByPrimaryKey(id);

    }

    @Override
    public List<ItemCat> findAll() {
        List<ItemCat> itemCats = itemCatDao.selectByExample(null);
        System.out.println("----------");




        return itemCats;
    }
}
