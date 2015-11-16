package com.systekcn.guide.biz;

import java.util.List;

/**
 * Created by Qiang on 2015/10/22.
 */
public interface InterfaceGetData {
    <T> List<T> getDataList(Class<?> entityType);
}
