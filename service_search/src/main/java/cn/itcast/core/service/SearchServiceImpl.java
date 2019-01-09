package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map paramMap) {
        //排序
        Map map = highsearch(paramMap);
        //分类去重
        List<String> categotryGroup = findCategotryGroup(paramMap);
        map.put("categoryList", categotryGroup);
        //取出传入参数分类，判断是否页面传入，若没有，默认取第一个并进行查询
        String categoryName = String.valueOf(paramMap.get("category"));

        if (categoryName != null && !"".equals(categoryName)) {
            Map brandListAndSpecListByCategoryName = findBrandListAndSpecListByCategoryName(categoryName);
            map.putAll(brandListAndSpecListByCategoryName);
        } else {
            Map brandListAndSpecListByCategoryName = findBrandListAndSpecListByCategoryName(categotryGroup.get(0));
            map.putAll(brandListAndSpecListByCategoryName);
        }
        return map;
    }

    private Map highsearch(Map paramMap) {
        //获取查询的关键字
        String keywords = String.valueOf(paramMap.get("keywords"));
        if (keywords != null) {
            keywords = keywords.replaceAll(" ", "");
        }
        //获取当前页
        Integer pageNo = Integer.parseInt(String.valueOf(paramMap.get("pageNo")));
        //获取每页展示多少条数据
        Integer pageSize = Integer.parseInt(String.valueOf(paramMap.get("pageSize")));
        //获取用户选中的分类过滤条件
        String category = String.valueOf(paramMap.get("category"));
        //获取用户选中的品牌过滤条件
        String brand = String.valueOf(paramMap.get("brand"));
        //获取用户选中的规格过滤条件
        String spec = String.valueOf(paramMap.get("spec"));
        //价格
        String price = String.valueOf(paramMap.get("price"));
        //升序降序
        String sortField = String.valueOf(paramMap.get("sortField"));
        String sort = String.valueOf(paramMap.get("sort"));

        Map<String, Object> map = new HashMap();

        HighlightQuery highlightQuery = new SimpleHighlightQuery();
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        highlightQuery.addCriteria(criteria);

        if (pageNo == null || pageNo < 0) {
            pageNo = 1;
        }
        //起始页
        Integer start = (pageNo - 1) * pageSize;
        //设置分页条数
        highlightQuery.setOffset(start);
        highlightQuery.setRows(pageSize);
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<em style=\"color:red\">");
        highlightOptions.setSimplePostfix("</em>");
        highlightQuery.setHighlightOptions(highlightOptions);

        //按照分类过滤
        if (category != null && !"".equals(category)) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria criteriaFilterQuery = new Criteria("item_category").is(category);
            filterQuery.addCriteria(criteriaFilterQuery);
            highlightQuery.addFilterQuery(filterQuery);
        }

        //按照品牌过滤
        if (brand != null && !"".equals(brand)) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria criteriaFilterQuery = new Criteria("item_brand").is(brand);
            filterQuery.addCriteria(criteriaFilterQuery);
            highlightQuery.addFilterQuery(filterQuery);
        }

        //按照规格过滤
        if (spec != null && !"".equals(spec)) {
            Map<String, String> map1 = JSON.parseObject(spec, Map.class);
            if (map1 != null && map1.size() > 0) {
                Set<Map.Entry<String, String>> entries = map1.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    //创建过滤查询对象
                    FilterQuery filterQuery = new SimpleFilterQuery();
                    //创建过滤条件对象
                    Criteria filterCriTeria = new Criteria("item_spec_" + entry.getKey()).is(entry.getValue());
                    //将过滤条件加入到过滤查询对象中
                    filterQuery.addCriteria(filterCriTeria);
                    //将过滤查询对象加入到查询对象中
                    highlightQuery.addFilterQuery(filterQuery);

                }

            }

        }

        //按照价格查询
        if (price != null && !"".equals(price)) {
            String[] split = price.split("-");

            if (!"0".equals(split[0])) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria criteriaFilterQuery = new Criteria("item_price").greaterThanEqual(split[0]);
                filterQuery.addCriteria(criteriaFilterQuery);
                highlightQuery.addFilterQuery(filterQuery);
            }
            if (!"*".equals(split[1])) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria criteriaFilterQuery = new Criteria("item_price").lessThanEqual(split[1]);
                filterQuery.addCriteria(criteriaFilterQuery);
                highlightQuery.addFilterQuery(filterQuery);
            }

        }

        //升序降序
        if (sortField != null && sort != null && !"".equals(sortField) && !"".equals(sort)) {
            //升序
            if ("ASC".equals(sort)) {
                //创建排序对象, 第一个参数排序方式, 第二个参数:排序的域名
                Sort sort1 = new Sort(Sort.Direction.ASC, "item_" + sortField);
                //将排序对象放入查询对象中
                highlightQuery.addSort(sort1);
            }
            //降序
            if ("DESC".equals(sort)) {
                Sort sort1 = new Sort(Sort.Direction.DESC, "item_" + sortField);
                highlightQuery.addSort(sort1);
            }
        }

        //查询结果
        HighlightPage<Item> items = solrTemplate.queryForHighlightPage(highlightQuery, Item.class);
        List<HighlightEntry<Item>> highlighted = items.getHighlighted();
        //存储遍历之后的带有高亮的item对象的集合
        List<Item> itemList = new ArrayList<>();
        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
            //查找到的单个item对象
            Item item = itemHighlightEntry.getEntity();

            if (itemHighlightEntry.getHighlights() != null && itemHighlightEntry.getHighlights().size() > 0) {
                List<String> snipplets = itemHighlightEntry.getHighlights().get(0).getSnipplets();
                if (snipplets != null && snipplets.size() > 0) {
                    String s = snipplets.get(0);
                    item.setTitle(s);
                }

            }
            itemList.add(item);
        }
        map.put("rows", itemList);
        //总条数
        map.put("total", items.getTotalElements());
        //总页数
        map.put("totalPages", items.getTotalPages());
        return map;
    }

    private List<String> findCategotryGroup(Map paramMap) {
        List<String> resultList = new ArrayList<>();
        String keywords = String.valueOf(paramMap.get("keywords"));
        if (keywords != null) {
            keywords = keywords.replaceAll(" ", "");
        }
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        query.addCriteria(criteria);
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        GroupPage<Item> items = solrTemplate.queryForGroupPage(query, Item.class);
        GroupResult<Item> item_category = items.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = item_category.getGroupEntries();
        //遍历分组集合数据
        if (groupEntries != null) {
            for (GroupEntry<Item> groupEntry : groupEntries) {
                //获取分组后的分类的名称
                String categoryGroupValue = groupEntry.getGroupValue();
                resultList.add(categoryGroupValue);
            }
        }

        return resultList;
    }

    private Map findBrandListAndSpecListByCategoryName(String categoryName) {
        Long categoryId = (Long) redisTemplate.boundHashOps(Constants.CATEGORY_LIST_REDIS).get(categoryName);
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps(Constants.BRAND_LIST_REDIS).get(categoryId);
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps(Constants.SPEC_LIST_REDIS).get(categoryId);
        Map map = new HashMap();
        map.put("brandList", brandList);
        map.put("specList", specList);
        return map;
    }

}
