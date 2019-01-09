package cn.itcast.core.service.impl;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class SellerServiceImpl implements SellerService {

    @Autowired
    private SellerDao sellerDao;

    @Override
    public void add(Seller seller) {
        seller.setStatus("0");
        seller.setCreateTime(new Date());
        sellerDao.insert(seller);
    }

    @Override
    public Seller findOne(String id) {
        return sellerDao.selectByPrimaryKey(id);

    }

    @Override
    public void updateStatus(String sellerId, String status) {
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }

    @Override
    public PageResult findPage(Seller seller, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        SellerQuery query = new SellerQuery();
        SellerQuery.Criteria criteria = query.createCriteria();
        if (seller != null) {
            if (seller.getStatus() != null && !"".equals(seller.getStatus())) {
                criteria.andStatusEqualTo(seller.getStatus());
            }
            if (seller.getName() != null && !"".equals(seller.getName())) {
                criteria.andNameLike("%" + seller.getName() + "%");
            }
            if (seller.getNickName() != null && !"".equals(seller.getNickName())) {
                criteria.andNickNameLike("%" + seller.getNickName() + "%");
            }
        }

        Page<Seller> sellerList = (Page<Seller>) sellerDao.selectByExample(query);
        System.out.println(sellerList.getTotal());
        return new PageResult(sellerList.getTotal(), sellerList.getResult());
    }
}
