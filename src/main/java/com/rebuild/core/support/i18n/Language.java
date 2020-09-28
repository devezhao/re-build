/*
Copyright (c) REBUILD <https://getrebuild.com/> and/or its owners. All rights reserved.

rebuild is dual-licensed under commercial and open source licenses (GPLv3).
See LICENSE and COMMERCIAL in the project root for license information.
*/

package com.rebuild.core.support.i18n;

import cn.devezhao.persist4j.Entity;
import cn.devezhao.persist4j.Field;
import cn.devezhao.persist4j.metadata.BaseMeta;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rebuild.core.*;
import com.rebuild.core.metadata.MetadataHelper;
import com.rebuild.core.metadata.impl.DisplayType;
import com.rebuild.core.support.ConfigurationItem;
import com.rebuild.core.support.RebuildConfiguration;
import com.rebuild.core.support.state.StateSpec;
import com.rebuild.utils.CommonsUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 多语言
 *
 * @author ZHAO
 * @since 2019/10/31
 */
@Component
public class Language implements Initialization {

    private static final Logger LOG = LoggerFactory.getLogger(Language.class);

    private static final String BUNDLE_FILE = "i18n/language.%s.json";

    /**
     * 手动刷新
     */
    public static final ThreadLocal<Boolean> MANUAL_REFRESH = new NamedThreadLocal<>("Manual refresh on batch");

    private Map<String, LanguageBundle> bundleMap = new HashMap<>();

    @Override
    public void init() {
        String[] supports = BootEnvironmentPostProcessor.getProperty(
                "rebuild.SuportLanguages", "zh_CN,en").split(",");

        for (String locale : supports) {
            LOG.info("Loading language bundle : " + locale);

            try (InputStream is = CommonsUtils.getStreamOfRes(String.format(BUNDLE_FILE, locale))) {
                JSONObject o = JSON.parseObject(is, null);
                LanguageBundle bundle = new LanguageBundle(locale, o, this);
                bundleMap.put(locale, bundle);
            } catch (IOException ex) {
                LOG.error("Cannot load language bundle : " + locale, ex);
            }
        }
    }

    /**
     * 刷新语言包
     *
     * @param force
     */
    public void refresh(boolean force) {
        if (bundleMap.isEmpty()) return;

        if (force) MANUAL_REFRESH.remove();

        Boolean manual = MANUAL_REFRESH.get();
        if (manual != null && manual) return;

        try {
            this.init();
        } catch (Exception e) {
            LOG.error("Refresh language-bundle error", e);
        }
    }

    /**
     * @param locale
     * @return
     * @see java.util.Locale
     */
    public LanguageBundle getBundle(String locale) {
        if (locale != null) {
            if (bundleMap.containsKey(locale)) {
                return bundleMap.get(locale);
            }

            locale = useLanguageCode(locale);
            if (locale != null) {
                return bundleMap.get(locale);
            }
        }

        return getDefaultBundle();
    }

    /**
     * 默认语言包
     *
     * @return
     */
    public LanguageBundle getDefaultBundle() {
        String d = RebuildConfiguration.get(ConfigurationItem.DefaultLanguage);
        if (available(d) == null) {
            throw new RebuildException("No default locale found : " + d);
        }
        return bundleMap.get(d);
    }

    /**
     * @param locale
     * @return
     */
    private String useLanguageCode(String locale) {
        String code = locale.split("[_-]")[0];
        for (String key : bundleMap.keySet()) {
            if (key.equals(code) || key.startsWith(code)) {
                return key;
            }
        }
        return null;
    }

    /**
     * 是否为可用语言
     *
     * @param locale
     * @return
     */
    public String available(String locale) {
        boolean a = bundleMap.containsKey(locale);
        if (a) return locale;

        if ((locale = useLanguageCode(locale)) != null) {
            return locale;
        }
        return null;
    }

    /**
     * @return
     */
    public Set<String> availableLocales() {
        return bundleMap.keySet();
    }

    // -- Quick Methods

    /**
     * 当前用户语言包（线程量用户）
     *
     * @return
     * @see UserContextHolder#getLocale()
     * @see com.rebuild.utils.AppUtils#getReuqestBundle(HttpServletRequest)
     */
    public static LanguageBundle getCurrentBundle() {
        return Application.getLanguage().getBundle(UserContextHolder.getLocale());
    }

    /**
     * @param key
     * @param phKeys 可替换语言 Key 中的 {0} {1}
     * @return
     */
    public static String getLang(String key, String... phKeys) {
        return getCurrentBundle().getLang(key, phKeys);
    }

    /**
     * @param key
     * @param phValues 可格式化语言 Key 中的 %s %d
     * @return
     */
    public static String formatLang(String key, Object... phValues) {
        return getCurrentBundle().formatLang(key, phValues);
    }

    /**
     * 元数据语言
     *
     * @param entityOrField
     * @return
     */
    public static String getLang(BaseMeta entityOrField) {
        String langKey;
        if (entityOrField instanceof Entity) {
            langKey = LanguageBundle.PREFIX_ENTITY + entityOrField.getName();
        } else {
            Field field = (Field) entityOrField;
            if (MetadataHelper.isCommonsField(field)) {
                langKey = LanguageBundle.PREFIX_FIELD + field.getName();
            } else {
                langKey = LanguageBundle.PREFIX_FIELD + field.getOwnEntity().getName() + "." + field.getName();
            }
        }

        return StringUtils.defaultIfBlank(
                getCurrentBundle().getLangBase(langKey), entityOrField.getDescription());
    }

    /**
     * 状态语言
     *
     * @param state
     * @return
     */
    public static String getLang(StateSpec state) {
        String langKey = LanguageBundle.PREFIX_STATE + state.getClass().getSimpleName() + "." + ((Enum<?>) state).name();
        return StringUtils.defaultIfBlank(getCurrentBundle().getLangBase(langKey), state.getName());
    }

    /**
     * 字段类型语言
     *
     * @param type
     * @return
     */
    public static String getLang(DisplayType type) {
        String langKey = LanguageBundle.PREFIX_DISPLAY_TYPE + type.name();
        return StringUtils.defaultIfBlank(getCurrentBundle().getLangBase(langKey), type.getDisplayName());
    }
}
