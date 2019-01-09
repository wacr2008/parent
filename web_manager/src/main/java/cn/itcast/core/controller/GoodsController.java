package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

   /* @Reference
    private SolrManagerService solrManagerService;

    @Reference
    CmsService cmsService;*/

    @RequestMapping("/search")
    public PageResult search(@RequestBody Goods goods, Integer page, Integer rows) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        //将当前用户名放入卖家id字段
        goods.setSellerId(userName);
        return goodsService.search(goods, page, rows);
    }

    @RequestMapping("/findOne")
    public GoodsEntity findOne(Long id) {
        return goodsService.findOne(id);

    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            if (ids != null) {
                for (Long id : ids) {
                    goodsService.delete(id);
               //     solrManagerService.deleteItemFromSolr(id);
                }
            }

            return new Result(true, "删除成功!!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败!!");
        }
    }

    /**
     * 修改商品状态
     *
     * @param ids    商品id
     * @param status 商品状态: 0未审核, 1审核通过, 2驳回
     * @return
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            if (ids != null) {
                for (Long id : ids) {
                    goodsService.updateStatus(id, status);
                   /* if ("1".equals(status)) {
                        solrManagerService.saveItemToSolr(id);
                        Map<String, Object> dataById = cmsService.findDataById(id);
                        cmsService.createStaticPage(dataById, id);
                    }*/

                }

            }
            return new Result(true, "状态修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "状态修改失败!");
        }
    }
/*@RequestMapping("/test")
    public String get(Long id){
        try {
            Map<String, Object> dataById = cmsService.findDataById(id);
            cmsService.createStaticPage(dataById, id);
            return "true";
        } catch ( Exception e) {
            e.printStackTrace();
            return "false";
        }
    }*/

}
