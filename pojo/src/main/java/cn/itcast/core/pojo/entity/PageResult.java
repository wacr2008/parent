package cn.itcast.core.pojo.entity;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable {
    //总记录数
    private Long total;
    //当前页结果
    private List rows;
    //总活跃人数
    private Integer activecount;
    //用户总人数
    private Integer allUser;

    public Integer getAllUser() {
        return allUser;
    }

    public void setAllUser(Integer allUser) {
        this.allUser = allUser;
    }

    public Integer getActivecount() {
        return activecount;
    }

    public void setActivecount(Integer activecount) {
        this.activecount = activecount;
    }

    public PageResult(Long total, List rows, Integer activecount) {
        this.total = total;
        this.rows = rows;
        this.activecount = activecount;
    }

    public PageResult(Long total, List rows) {
        this.total = total;
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}
