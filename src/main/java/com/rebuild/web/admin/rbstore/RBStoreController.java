/*
Copyright (c) REBUILD <https://getrebuild.com/> and/or its owners. All rights reserved.

rebuild is dual-licensed under commercial and open source licenses (GPLv3).
See LICENSE and COMMERCIAL in the project root for license information.
*/

package com.rebuild.web.admin.rbstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rebuild.api.RespBody;
import com.rebuild.core.metadata.MetadataHelper;
import com.rebuild.core.rbstore.RBStore;
import com.rebuild.web.BaseController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author devezhao-mbp zhaofang123@gmail.com
 * @since 2019/04/28
 */
@RestController
@RequestMapping("/admin/rbstore")
public class RBStoreController extends BaseController {

    @GetMapping("load-index")
    public Object loadDataIndex(HttpServletRequest request) {
        String type = getParameterNotNull(request, "type");
        JSON index = RBStore.fetchRemoteJson(type + "/index.json");
        return index == null ? RespBody.error() : index;
    }

    @GetMapping("load-metaschemas")
    public JSON loadMetaschemas() {
        JSONArray index = (JSONArray) RBStore.fetchMetaschema("index.json");

        for (Object o : index) {
            JSONObject item = (JSONObject) o;
            String key = item.getString("key");
            if (MetadataHelper.containsEntity(key)) {
                item.put("exists", true);
            }
        }
        return index;
    }
}
