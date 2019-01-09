package cn.itcast.core.service;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;

public interface CmsService {
    void createStaticPage(Map<String, Object> map, Long id) throws IOException, TemplateException;
    Map<String, Object> findDataById(Long id);
}
