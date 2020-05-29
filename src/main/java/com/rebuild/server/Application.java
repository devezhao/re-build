/*
Copyright (c) REBUILD <https://getrebuild.com/> and its owners. All rights reserved.

rebuild is dual-licensed under commercial and open source licenses (GPLv3).
See LICENSE and COMMERCIAL in the project root for license information.
*/

package com.rebuild.server;

import cn.devezhao.bizz.security.AccessDeniedException;
import cn.devezhao.commons.ReflectUtils;
import cn.devezhao.commons.excel.Cell;
import cn.devezhao.persist4j.PersistManagerFactory;
import cn.devezhao.persist4j.Query;
import cn.devezhao.persist4j.engine.ID;
import cn.devezhao.persist4j.engine.StandardRecord;
import cn.devezhao.persist4j.query.QueryedRecord;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.rebuild.api.ApiGateway;
import com.rebuild.api.BaseApi;
import com.rebuild.server.helper.AesPreferencesConfigurer;
import com.rebuild.server.helper.ConfigurableItem;
import com.rebuild.server.helper.License;
import com.rebuild.server.helper.SysConfiguration;
import com.rebuild.server.helper.cache.CommonCache;
import com.rebuild.server.helper.cache.RecentlyUsedCache;
import com.rebuild.server.helper.cache.RecordOwningCache;
import com.rebuild.server.helper.setup.UpgradeDatabase;
import com.rebuild.server.metadata.DynamicMetadataFactory;
import com.rebuild.server.service.CommonService;
import com.rebuild.server.service.EntityService;
import com.rebuild.server.service.SQLExecutor;
import com.rebuild.server.service.ServiceSpec;
import com.rebuild.server.service.base.GeneralEntityService;
import com.rebuild.server.service.bizz.privileges.UserStore;
import com.rebuild.server.service.notification.NotificationService;
import com.rebuild.server.service.query.QueryFactory;
import com.rebuild.utils.RbDateCodec;
import com.rebuild.utils.RbRecordCodec;
import com.rebuild.web.OnlineSessionStore;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.Driver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 后台类入口
 * 
 * @author zhaofang123@gmail.com
 * @since 05/18/2018
 */
public final class Application {
	
	/** Rebuild Version
	 */
	public static final String VER = "1.10.0-dev";
	/** Rebuild Build
	 */
	public static final int BUILD = 011000;

	/** Logging for Global
	 */
	public static final Log LOG = LogFactory.getLog(Application.class);

	static {
		// Driver for DB
		try {
			Class.forName(com.mysql.jdbc.Driver.class.getName());
			Class.forName(Driver.class.getName());
		} catch (ClassNotFoundException ex) {
			throw new RebuildException(ex);
		}

		// for fastjson Serialize
		SerializeConfig.getGlobalInstance().put(ID.class, ToStringSerializer.instance);
		SerializeConfig.getGlobalInstance().put(Date.class, RbDateCodec.instance);
		SerializeConfig.getGlobalInstance().put(StandardRecord.class, RbRecordCodec.instance);
		SerializeConfig.getGlobalInstance().put(QueryedRecord.class, RbRecordCodec.instance);
		SerializeConfig.getGlobalInstance().put(Cell.class, ToStringSerializer.instance);
	}
	
	// 调试模式/开发模式
	private static boolean debugMode = false;
	// 服务启动正常
	private static boolean serversReady = false;
	
	// SPRING
	private static ApplicationContext APPLICATION_CTX;
	// 实体对应的服务类
	private static Map<Integer, ServiceSpec> SSS = null;
	
	/**
	 * @param ctx
	 */
	protected Application(ApplicationContext ctx) {
		APPLICATION_CTX = ctx;
	}

	/**
	 * 初始化
	 *
	 * @param startAt
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	synchronized
	protected void init(long startAt) throws Exception {
		serversReady = ServerStatus.checkAll();
		if (!serversReady) {
		    LOG.fatal(formatFailure("REBUILD BOOTING FAILURE DURING THE STATUS CHECK.", "PLEASE VIEW LOGS FOR MORE DETAILS."));
			return;
		}

		try {
			// 升级数据库
			UpgradeDatabase.getInstance().upgradeQuietly();

			// 刷新配置缓存
			for (ConfigurableItem item : ConfigurableItem.values()) {
				SysConfiguration.get(item, true);
			}

			// 自定义实体
			LOG.info("Loading customized/business entities ...");
			((DynamicMetadataFactory) APPLICATION_CTX.getBean(PersistManagerFactory.class).getMetadataFactory()).refresh(false);

			// 实体对应的服务类
			SSS = new HashMap<>(16);
			for (Map.Entry<String, ServiceSpec> e : APPLICATION_CTX.getBeansOfType(ServiceSpec.class).entrySet()) {
				ServiceSpec ss = e.getValue();
				if (ss.getEntityCode() > 0) {
					SSS.put(ss.getEntityCode(), ss);
					LOG.info("Service specification : " + ss);
				}
			}

			// 注册 API
			Set<Class<?>> apiClasses = ReflectUtils.getAllSubclasses(ApiGateway.class.getPackage().getName(), BaseApi.class);
			for (Class<?> c : apiClasses) {
				ApiGateway.registerApi((Class<? extends BaseApi>) c);
			}

			if (APPLICATION_CTX instanceof AbstractApplicationContext) {
                ((AbstractApplicationContext) APPLICATION_CTX).registerShutdownHook();
            }

			LOG.info("Rebuild Boot successful in " + (System.currentTimeMillis() - startAt) + " ms");

		} catch (Exception ex) {
			serversReady = false;
			throw ex;
		}
	}

    /**
     * 格式化重大消息
     *
     * @param msgs
     * @return
     */
    protected static String formatFailure(String...msgs) {
		List<String> msgsList = new ArrayList<>();
		CollectionUtils.addAll(msgsList, msgs);
		msgsList.add("\n  Version : " + VER);
		msgsList.add("OS      : " + SystemUtils.OS_NAME + " " + SystemUtils.OS_ARCH);
		msgsList.add("Report an issue :");
		msgsList.add("https://getrebuild.com/report-issue?title=failure");

        return "\n###################################################################\n\n  "
                + StringUtils.join(msgsList, "\n  ") +
                "\n\n###################################################################";
    }

	/**
	 * FOR TESTING ONLY
	 *
	 * @return
	 * @throws Exception
	 */
	synchronized
	protected static ApplicationContext debug() throws Exception {
		if (APPLICATION_CTX == null) {
			debugMode = true;
			LOG.info("Rebuild Booting in DEBUG mode ...");

			AesPreferencesConfigurer.initApplicationProperties();
			long at = System.currentTimeMillis();
			ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] { "application-ctx.xml" });
			new Application(ctx).init(at);
		}
		return APPLICATION_CTX;
	}
	
	/**
	 * 是否开发模式
	 * @return
	 */
	public static boolean devMode() {
		return BooleanUtils.toBoolean(System.getProperty("rbdev")) || debugMode;
	}

	/**
	 * 是否商业授权
	 * @return
	 */
	public static boolean rbvMode() {
		return BooleanUtils.toBoolean(System.getProperty("rbv")) || License.isCommercial();
	}
	
	/**
	 * 各项服务是否正常启动
	 * @return
	 */
    public static boolean serversReady() {
		return serversReady;
	}
	
	/**
	 * 添加服务停止钩子
	 * @param hook
	 */
	public static void addShutdownHook(Thread hook) {
		LOG.info("Add shutdown hook : " + hook.getName());
		Runtime.getRuntime().addShutdownHook(hook);
	}
	
	/**
	 * SPRING Context
	 * @return
	 */
	public static ApplicationContext getApplicationContext() {
		if (APPLICATION_CTX == null) {
			throw new IllegalStateException("Rebuild unstarted");
		}
		return APPLICATION_CTX;
	}

	/**
	 * @param beanClazz
	 * @param <T>
	 * @return
	 */
	public static <T> T getBean(Class<T> beanClazz) {
		return getApplicationContext().getBean(beanClazz);
	}

	/**
	 * @return
	 */
	public static OnlineSessionStore getSessionStore() {
		return getBean(OnlineSessionStore.class);
	}

	/**
	 * @return
	 * @throws AccessDeniedException
	 * @see #getUserStore()
	 */
	public static ID getCurrentUser() throws AccessDeniedException {
		return getSessionStore().get();
	}

	/**
	 * @return
	 */
	public static PersistManagerFactory getPersistManagerFactory() {
		return getBean(PersistManagerFactory.class);
	}

	/**
	 * @return
	 */
	public static DynamicMetadataFactory getMetadataFactory() {
		return (DynamicMetadataFactory) getPersistManagerFactory().getMetadataFactory();
	}

	/**
	 * @return
	 */
	public static UserStore getUserStore() {
		return getBean(UserStore.class);
	}

	/**
	 * @return
	 */
	public static RecordOwningCache getRecordOwningCache() {
		return getBean(RecordOwningCache.class);
	}

	/**
	 * @return
	 */
	public static RecentlyUsedCache getRecentlyUsedCache() {
		return getBean(RecentlyUsedCache.class);
	}

	/**
	 * @return
	 */
	public static CommonCache getCommonCache() {
		return getBean(CommonCache.class);
	}

	/**
	 * @return
	 */
	public static com.rebuild.server.service.bizz.privileges.SecurityManager getSecurityManager() {
		return getBean(com.rebuild.server.service.bizz.privileges.SecurityManager.class);
	}

	/**
	 * @return
	 */
	public static QueryFactory getQueryFactory() {
		return getBean(QueryFactory.class);
	}

	/**
	 * @param ajql
	 * @return
	 */
	public static Query createQuery(String ajql) {
		return getQueryFactory().createQuery(ajql);
	}

	/**
	 * @param ajql
	 * @param user
	 * @return
	 */
	public static Query createQuery(String ajql, ID user) {
		return getQueryFactory().createQuery(ajql, user);
	}

	/**
	 * @param ajql
	 * @return
	 */
	public static Query createQueryNoFilter(String ajql) {
		return getQueryFactory().createQueryNoFilter(ajql);
	}

	/**
	 * @return
	 */
	public static SQLExecutor getSQLExecutor() {
		return getBean(SQLExecutor.class);
	}

	/**
	 * @return
	 */
	public static NotificationService getNotifications() {
		return getBean(NotificationService.class);
	}

	/**
	 * @param entityCode
	 * @return
	 */
	public static ServiceSpec getService(int entityCode) {
		if (SSS != null && SSS.containsKey(entityCode)) {
			return SSS.get(entityCode);
		} else {
			return getGeneralEntityService();
		}
	}
	
	/**
	 * 业务实体服务专用
	 * @param entityCode
	 * @return
	 * @see #getGeneralEntityService()
	 */
	public static EntityService getEntityService(int entityCode) {
		ServiceSpec spec = getService(entityCode);
		if (EntityService.class.isAssignableFrom(spec.getClass())) {
			return (EntityService) spec;
		}
		throw new RebuildException("Non EntityService implements : " + entityCode);
	}
	
	/**
	 * @return
	 */
	public static GeneralEntityService getGeneralEntityService() {
		return (GeneralEntityService) getApplicationContext().getBean("generalEntityService");
	}
	
	/**
	 * @return
	 */
	public static CommonService getCommonService() {
		return getBean(CommonService.class);
	}
}
