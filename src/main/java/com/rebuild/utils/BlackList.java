/*
Copyright (c) REBUILD <https://getrebuild.com/> and/or its owners. All rights reserved.

rebuild is dual-licensed under commercial and open source licenses (GPLv3).
See LICENSE and COMMERCIAL in the project root for license information.
*/

package com.rebuild.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.rebuild.core.Application;
import org.apache.commons.lang.ArrayUtils;

/**
 * 黑名单词 src/main/resources/blacklist.json
 * More details https://github.com/fighting41love/funNLP
 *
 * @author devezhao
 * @since 01/31/2019
 */
public class BlackList {

    private static JSONArray BLACKLIST = null;

    /**
     * 是否黑名单关键词
     *
     * @param text
     * @return
     */
    public static boolean isBlack(String text) {
        if (BLACKLIST == null) {
            String s = CommonsUtils.getStringOfRes("blacklist.json");
            BLACKLIST = JSON.parseArray(s == null ? JSONUtils.EMPTY_ARRAY_STR : s);
        }

        if (Application.devMode()) {
            return isSqlKeyword(text);
        } else {
            return BLACKLIST.contains(text.toLowerCase()) || isSqlKeyword(text);
        }
    }

    /**
     * 是否 SQL 关键词
     *
     * @param text
     * @return
     */
    public static boolean isSqlKeyword(String text) {
        return ArrayUtils.contains(SQL_KWS, text.toUpperCase());
    }

    // SQL 关键字
    private static final String[] SQL_KWS = new String[]{
            "SELECT", "DISTINCT", "MAX", "MIN", "AVG", "SUM", "COUNT", "FROM",
            "WHERE", "AND", "OR", "ORDER", "BY", "ASC", "DESC", "GROUP", "HAVING",
            "WITH", "ROLLUP", "IS", "NOT", "NULL", "IN", "LIKE", "EXISTS", "BETWEEN", "TRUE", "FALSE"
    };
}
