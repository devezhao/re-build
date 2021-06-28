/*
Copyright (c) REBUILD <https://getrebuild.com/> and/or its owners. All rights reserved.

rebuild is dual-licensed under commercial and open source licenses (GPLv3).
See LICENSE and COMMERCIAL in the project root for license information.
*/

package com.rebuild.web.robot.trigger;

import cn.devezhao.persist4j.Entity;
import cn.devezhao.persist4j.Field;
import com.alibaba.fastjson.JSON;
import com.rebuild.core.metadata.MetadataHelper;
import com.rebuild.core.metadata.MetadataSorter;
import com.rebuild.core.metadata.easymeta.DisplayType;
import com.rebuild.core.metadata.easymeta.EasyField;
import com.rebuild.core.metadata.easymeta.EasyMetaFactory;
import com.rebuild.core.service.approval.RobotApprovalManager;
import com.rebuild.core.service.trigger.impl.FieldWriteback;
import com.rebuild.utils.JSONUtils;
import com.rebuild.web.BaseController;
import com.rebuild.web.EntityParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * @author devezhao zhaofang123@gmail.com
 * @since 2019/05/25
 */
@RestController
@RequestMapping("/admin/robot/trigger/")
public class FieldAggregationController extends BaseController {

    /**
     * @see FieldWritebackController#getTargetEntities(Entity)
     */
    @RequestMapping("field-aggregation-entities")
    public List<String[]> getTargetEntities(@EntityParam(name = "source") Entity sourceEntity) {
        List<String[]> entities = new ArrayList<>();
        for (Field refField : MetadataSorter.sortFields(sourceEntity, DisplayType.REFERENCE)) {
            if (MetadataHelper.isApprovalField(refField.getName())) {
                continue;
            }

            Entity refEntity = refField.getReferenceEntity();
            String entityLabel = EasyMetaFactory.getLabel(refEntity) + " (" + EasyMetaFactory.getLabel(refField) + ")";
            entities.add(new String[] { refEntity.getName(), entityLabel, refField.getName() });
        }

        Comparator<Object> comparator = Collator.getInstance(Locale.CHINESE);
        entities.sort((o1, o2) -> comparator.compare(o1[1], o2[1]));

        // 可更新自己（通过主键字段）
        entities.add(new String[] {
                sourceEntity.getName(), EasyMetaFactory.getLabel(sourceEntity), FieldWriteback.SOURCE_SELF });

        return entities;
    }

    @RequestMapping("field-aggregation-fields")
    public JSON getTargetFields(@EntityParam(name = "source") Entity sourceEntity,
                                HttpServletRequest request) {
        String target = getParameter(request, "target");
        Entity targetEntity = StringUtils.isBlank(target) ? null : MetadataHelper.getEntity(target);

        List<String[]> sourceFields = new ArrayList<>();
        List<String[]> targetFields = new ArrayList<>();

        // 源字段

        // 本实体
        for (Field field : MetadataSorter.sortFields(sourceEntity)) {
            if (EasyMetaFactory.getDisplayType(field) == DisplayType.BARCODE) continue;
            sourceFields.add(buildField(field));
        }

        // 关联实体
        for (Field fieldRef : MetadataSorter.sortFields(sourceEntity, DisplayType.REFERENCE)) {
            String fieldRefName = fieldRef.getName() + ".";
            String fieldRefLabel = EasyMetaFactory.getLabel(fieldRef) + ".";

            for (Field field : MetadataSorter.sortFields(fieldRef.getReferenceEntity())) {
                if (EasyMetaFactory.getDisplayType(field) == DisplayType.BARCODE) continue;

                String[] build = buildField(field);
                build[0] = fieldRefName + build[0];
                build[1] = fieldRefLabel + build[1];
                sourceFields.add(build);
            }
        }

        // 目标字段

        if (targetEntity != null) {
            for (Field field : MetadataSorter.sortFields(targetEntity, DisplayType.NUMBER, DisplayType.DECIMAL)) {
                if (EasyMetaFactory.valueOf(field).isBuiltin()) continue;
                targetFields.add(buildField(field));
            }
        }

        // 审批流程启用
        boolean hadApproval = targetEntity != null
                && RobotApprovalManager.instance.hadApproval(targetEntity, null) != null;

        return JSONUtils.toJSONObject(
                new String[] { "source", "target", "hadApproval" },
                new Object[] {
                        sourceFields.toArray(new String[sourceFields.size()][]),
                        targetFields.toArray(new String[targetFields.size()][]),
                        hadApproval });
    }

    private String[] buildField(Field field) {
        EasyField easyField = EasyMetaFactory.valueOf(field);
        return new String[] { field.getName(), easyField.getLabel(), easyField.getDisplayType().name() };
    }
}
