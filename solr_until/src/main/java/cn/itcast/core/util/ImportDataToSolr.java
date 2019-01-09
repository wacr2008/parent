package cn.itcast.core.util;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ImportDataToSolr {
    @Autowired
    private ItemDao itemDao;

    @Autowired
    private SolrTemplate solrTemplate;


    public  void addDataToSolr(){
        ItemQuery itemQuery=new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andStatusEqualTo("1");
        List<Item> items = itemDao.selectByExample(itemQuery);
        if (items!=null){
            for (Item item : items) {
                //获取规格的json字符串
                String specJsonStr = item.getSpec();
                //将json转换成map
                Map map = JSON.parseObject(specJsonStr, Map.class);
                item.setSpecMap(map);
            }
            solrTemplate.saveBeans(items);
            solrTemplate.commit();

        }

    }

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        ImportDataToSolr bean =(ImportDataToSolr) applicationContext.getBean("importDataToSolr");
        bean.addDataToSolr();

    }

}
