package cn.itcast.core.service;

public interface SolrManagerService {
    void saveItemToSolr(Long id);

    void deleteItemFromSolr(Long id);
}
