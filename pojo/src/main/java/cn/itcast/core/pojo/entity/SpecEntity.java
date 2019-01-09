package cn.itcast.core.pojo.entity;

import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;

import java.io.Serializable;
import java.util.List;

public class SpecEntity implements Serializable {
    //规格对象
    private Specification specification;

    //规格选项集合
    private List<SpecificationOption> specificationOptionList;

    public SpecEntity(Specification specification, List<SpecificationOption> specificationOptionList) {
        this.specification = specification;
        this.specificationOptionList = specificationOptionList;
    }

    public SpecEntity() {
    }

    public Specification getSpecification() {
        return specification;
    }

    public void setSpecification(Specification specification) {
        this.specification = specification;
    }

    public List<SpecificationOption> getSpecificationOptionList() {
        return specificationOptionList;
    }

    public void setSpecificationOptionList(List<SpecificationOption> specificationOptionList) {
        this.specificationOptionList = specificationOptionList;
    }
}
