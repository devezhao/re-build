/*
Copyright (c) REBUILD <https://getrebuild.com/> and/or its owners. All rights reserved.

rebuild is dual-licensed under commercial and open source licenses (GPLv3).
See LICENSE and COMMERCIAL in the project root for license information.
*/

package com.rebuild.web.commons;

import cn.devezhao.commons.CodecUtils;
import com.alibaba.fastjson.JSON;
import com.rebuild.core.Application;
import com.rebuild.core.support.ConfigurationItem;
import com.rebuild.core.support.integration.QiniuCloud;
import com.rebuild.core.support.RebuildConfiguration;
import com.rebuild.utils.JSONUtils;
import com.rebuild.web.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * 文件共享
 *
 * @author ZHAO
 * @since 2019/9/26
 */
@Controller
public class FileShareController extends BaseController {

    // URL of public
    @GetMapping("/filex/make-url")
    @ResponseBody
    public JSON makeUrl(HttpServletRequest request) {
        String fileUrl = getParameterNotNull(request, "url");
        String publicUrl = makePublicUrl(fileUrl);
        return JSONUtils.toJSONObject("publicUrl", publicUrl);
    }

    // URL of share
    @GetMapping("/filex/make-share")
    @ResponseBody
    public JSON viewSharedFile(HttpServletRequest request) {
        Assert.isTrue(
                RebuildConfiguration.getBool(ConfigurationItem.FileSharable),
                getLang(request, "FileSharableDeny"));

        String fileUrl = getParameterNotNull(request, "url");
        int mtime = getIntParameter(request, "time", 5);

        String shareKey = CodecUtils.randomCode(40);
        Application.getCommonsCache().put(shareKey, fileUrl, mtime * 60);

        String shareUrl = RebuildConfiguration.getHomeUrl("s/" + shareKey);
        return JSONUtils.toJSONObject("shareUrl", shareUrl);
    }

    @GetMapping("/s/{shareKey}")
    public ModelAndView viewSharedFile(@PathVariable String shareKey,
                                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileUrl;
        if (!RebuildConfiguration.getBool(ConfigurationItem.FileSharable)
                || (fileUrl = Application.getCommonsCache().get(shareKey)) == null) {
            response.sendError(403, getLang(request, "ShardeFileExpired"));
            return null;
        }

        String publicUrl = makePublicUrl(fileUrl);
        return createModelAndView("/commons/shared-file", Collections.singletonMap("publicUrl", publicUrl));
    }

    /**
     * @param fileUrl
     * @return
     * @see FileDownloader#download(HttpServletRequest, HttpServletResponse)
     */
    private String makePublicUrl(String fileUrl) {
        String publicUrl;
        if (QiniuCloud.instance().available()) {
            publicUrl = QiniuCloud.instance().url(fileUrl, 120);
        } else {
            // @see FileDownloader#download
            String e = CodecUtils.randomCode(40);
            Application.getCommonsCache().put(e, "rb", 120);

            publicUrl = "filex/access/" + fileUrl + "?e=" + e;
            publicUrl = RebuildConfiguration.getHomeUrl(publicUrl);
        }
        return publicUrl;
    }
}
