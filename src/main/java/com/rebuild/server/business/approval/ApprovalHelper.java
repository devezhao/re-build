/*
Copyright (c) REBUILD <https://getrebuild.com/> and its owners. All rights reserved.

rebuild is dual-licensed under commercial and open source licenses (GPLv3).
See LICENSE and COMMERCIAL in the project root for license information.
*/

package com.rebuild.server.business.approval;

import cn.devezhao.persist4j.engine.ID;
import com.rebuild.server.Application;
import com.rebuild.server.helper.cache.NoRecordFoundException;
import com.rebuild.server.metadata.EntityHelper;
import com.rebuild.server.service.base.ApprovalStepService;
import org.springframework.util.Assert;

/**
 * @author devezhao
 * @since 2019/10/23
 */
public class ApprovalHelper {

    /**
     * 获取提交人
     *
     * @param record
     * @return
     */
    public static ID getSubmitter(ID record) {
        Object[] approvalId = Application.getQueryFactory().uniqueNoFilter(record, EntityHelper.ApprovalId);
        Assert.notNull(approvalId, "Couldn't found approval of record : " + record);
        return getSubmitter(record, (ID) approvalId[0]);
    }

    /**
     * 获取提交人
     *
     * @param record
     * @param approval
     * @return
     */
    public static ID getSubmitter(ID record, ID approval) {
        return Application.getBean(ApprovalStepService.class).getSubmitter(record, approval);
    }

    /**
     * @param recordId
     * @return Returns [ApprovalId, ApprovalName, ApprovalState, ApprovalStepNode]
     * @throws NoRecordFoundException
     */
    public static Object[] getApprovalState(ID recordId) throws NoRecordFoundException {
        Object[] o = Application.getQueryFactory().uniqueNoFilter(recordId,
                EntityHelper.ApprovalId, EntityHelper.ApprovalId + ".name", EntityHelper.ApprovalState, EntityHelper.ApprovalStepNode);
        if (o == null) {
            throw new NoRecordFoundException("记录不存在或你无权查看");
        }
        return o;
    }
}
