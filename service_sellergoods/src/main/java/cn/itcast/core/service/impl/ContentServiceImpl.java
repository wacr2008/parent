package cn.itcast.core.service.impl;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.service.ContentService;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContentServiceImpl implements ContentService {
    @Autowired
    private ContentDao contentDao;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public PageResult search(Content content, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        ContentQuery query = new ContentQuery();
        ContentQuery.Criteria criteria = query.createCriteria();
        if (content != null) {
            if (content.getTitle() != null && !"".equals(content.getTitle())) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
        }
        Page<Content> contentList = (Page<Content>) contentDao.selectByExample(query);
        return new PageResult(contentList.getTotal(), contentList.getResult());
    }

    /**
     * 添加
     * @param content
     */
    @Override
    public void add(Content content) {

        contentDao.insertSelective(content);
        redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(content.getCategoryId());
    }

    @Override
    public Content findOne(Long id) {
        return contentDao.selectByPrimaryKey(id);
    }

    /**
     * 修改
     * @param content
     */
    @Override
    public void update(Content content) {
        Content old = contentDao.selectByPrimaryKey(content.getId());
        redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(old.getCategoryId());
        redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(content.getCategoryId());
        contentDao.updateByPrimaryKeySelective(content);
    }

    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                Content content = contentDao.selectByPrimaryKey(id);
                contentDao.deleteByPrimaryKey(id);
                redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).get(content.getCategoryId());
            }
        }
    }

    @Override
    public List<Content> findByCategoryId(Long categoryId) {

        ContentQuery query = new ContentQuery();
        ContentQuery.Criteria criteria = query.createCriteria();
        criteria.andCategoryIdEqualTo(categoryId);
        List<Content> contents = contentDao.selectByExample(query);
        return contents;
    }

    @Override
    public List<Content> findByCategoryIdFromRedis(Long categoryId) {

        List<Content> contentList = (List<Content>) redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).get(categoryId);
        if (contentList == null) {
            ContentQuery query = new ContentQuery();
            ContentQuery.Criteria criteria = query.createCriteria();
            criteria.andCategoryIdEqualTo(categoryId);
            contentList = contentDao.selectByExample(query);
            redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).put(categoryId, contentList);
        }
        return contentList;
    }
}
