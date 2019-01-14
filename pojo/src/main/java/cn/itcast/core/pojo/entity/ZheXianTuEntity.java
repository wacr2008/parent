package cn.itcast.core.pojo.entity;

import java.util.Date;

public class ZheXianTuEntity {
    private String sellerName;
    private Date[] dates;

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public Date[] getDates() {
        return dates;
    }

    public void setDates(Date[] dates) {
        this.dates = dates;
    }
}