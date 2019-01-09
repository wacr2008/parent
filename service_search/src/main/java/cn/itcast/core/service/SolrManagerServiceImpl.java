package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
@Service
@Transactional
public class SolrManagerServiceImpl implements SolrManagerService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private ItemDao itemDao;

    @Override
    public void saveItemToSolr(Long id) {
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        if (itemList != null) {
            for (Item item : itemList) {
                String spec = item.getSpec();
                Map map = JSON.parseObject(spec,Map.class);
                item.setSpecMap(map);

            }
            solrTemplate.saveBeans(itemList);
            solrTemplate.commit();

        }

    }

    @Override
    public void deleteItemFromSolr(Long id) {
        Query query = new SimpleQuery();
        //创建条件对象
        Criteria criteria = new Criteria("item_goodsid").is(id);
        //将条件对象放入查询对象中
        query.addCriteria(criteria);

        //根据商品id, 条件进行删除
        solrTemplate.delete(query);
        //提交
        solrTemplate.commit();
    }
}
