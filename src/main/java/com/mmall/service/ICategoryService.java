package com.mmall.service;

import com.mmall.common.ServerResponse;

public interface ICategoryService {

    ServerResponse addCategory(String categoryName, Integer parentId);

}
