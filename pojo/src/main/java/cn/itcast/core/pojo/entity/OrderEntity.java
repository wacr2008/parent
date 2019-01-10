package cn.itcast.core.pojo.entity;

import java.io.Serializable;
import java.util.Date;

public class OrderEntity implements Serializable {
    private String status;
    private String finishedDate;
    private Date date;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(String finishedDate) {
        this.finishedDate = finishedDate;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
