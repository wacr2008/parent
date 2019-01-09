package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

public interface TemplateService {
    PageResult findPage(TypeTemplate template, Integer page, Integer rows);

    void add(TypeTemplate template);

    TypeTemplate findOne(Long id);

    void update(TypeTemplate template);

    void delete(Long[] ids);

    List<Map> findBySpecList(Long id);
}
