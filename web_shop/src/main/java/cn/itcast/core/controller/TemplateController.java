package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class TemplateController {
    @Reference
    TemplateService templateService;

    @RequestMapping("/search")
    public PageResult search(@RequestBody TypeTemplate template, Integer page, Integer rows) {
        return templateService.findPage(template, page, rows);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody TypeTemplate template) {
        try {
            templateService.add(template);
            return new Result(true, "添加成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败!");
        }

    }

    @RequestMapping("/findOne")
    public TypeTemplate findOne(Long id) {
        return templateService.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody TypeTemplate template) {
        try {
            templateService.update(template);
            return new Result(true, "修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败!");
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            templateService.delete(ids);
            return new Result(true, "删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败!");
        }
    }

    @RequestMapping("/findBySpecList")
    public List<Map> findBySpecList(Long id) {
        return templateService.findBySpecList(id);
    }

}








