package com.dute7liang.restful.common.resolver;

import com.dute7liang.restful.navigation.action.RestServiceItem;

import java.util.List;

/**
 * REST 服务解析器接口，负责从模块或项目中提取可导航的服务项。
 *
 * @author dute7liang
 */
public interface ServiceResolver {

    /**
     * 在模块范围内查找全部受支持的 REST 服务项。
     *
     * @return 模块级服务项列表
     */
    List<RestServiceItem> findAllSupportedServiceItemsInModule();

    /**
     * 在项目范围内查找全部受支持的 REST 服务项。
     *
     * @return 项目级服务项列表
     */
    List<RestServiceItem> findAllSupportedServiceItemsInProject();
}
