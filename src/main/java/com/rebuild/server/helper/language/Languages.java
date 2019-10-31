/*
rebuild - Building your business-systems freely.
Copyright (C) 2018-2019 devezhao <zhaofang123@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package com.rebuild.server.helper.language;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 多语言
 *
 * @author ZHAO
 * @since 2019/10/31
 */
public class Languages {

    private static final Log LOG = LogFactory.getLog(Languages.class);

    public static final Languages instance = new Languages();

    private Map<String, LanguageBundle> bundleMap = new HashMap<>();

    private Languages() {
        try {
            File[] files = ResourceUtils.getFile("classpath:locales/")
                    .listFiles((dir, name) -> name.startsWith("language-") && name.endsWith(".json"));
            for (File file : files) {
                String locale = file.getName().substring("language-".length());
                locale = locale.split("\\.")[0];

                try (InputStream is = new FileInputStream(file)) {
                    LOG.info("Loading language-bundle : " + locale);
                    JSONObject o = JSON.parseObject(is, null);
                    bundleMap.put(locale, new LanguageBundle(locale, o, this));
                }
            }
        } catch (IOException ex) {
            LOG.error("Load language-bundle failure!!!", ex);
        }
    }

    /**
     * @param locale
     * @return
     */
    public LanguageBundle getBundle(Locale locale) {
        return getBundle(locale == null ? null : locale.toString());
    }

    /**
     * @param locale
     * @return
     */
    public LanguageBundle getBundle(String locale) {
        if (locale != null && bundleMap.containsKey(locale)) {
            return bundleMap.get(locale);
        } else {
            return getDefaultBundle();
        }
    }

    /**
     * @return
     */
    public LanguageBundle getDefaultBundle() {
        return bundleMap.get("zh_CN");  // default
    }

    /**
     * @param key
     * @return
     */
    public String getLang(String key) {
        return getLang(key, null);
    }

    /**
     * @param key
     * @param locale
     * @return
     */
    public String getLang(String key, Locale locale) {
        return getBundle(locale).lang(key);
    }
}
