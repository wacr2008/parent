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



    @RequestMapping("/add")
    public Result add(@RequestBody GoodsEntity goodsEntity) {
        try {
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            goodsEntity.getGoods().setSellerId(userName);
            goodsService.add(goodsEntity);
            return new Result(true, "添加成功!!!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败!!!");

        }
    }

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

    @RequestMapping("/update")
    public Result update(@RequestBody GoodsEntity goodsEntity) {
        try {
            //判断当前用户是否有权限修改这个对象
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            //卖家id
            String sellerId = goodsEntity.getGoods().getSellerId();
            if (!userName.equals(sellerId)) {
                return new Result(false, "您没有权限修改这个商品!");
            }
            goodsService.update(goodsEntity);
            return new Result(true, "修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败!");
        }

    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            if (ids!=null){
                for (Long id : ids) {

                    goodsService.delete(id);
                }

            }
            return new Result(true, "删除成功!!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败!!");
        }
    }

}
