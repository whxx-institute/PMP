package org.jeecg.modules.project.protree.service.impl;

import org.jeecg.common.util.FillRuleUtil;
import org.jeecg.common.util.oConvertUtils;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.project.protree.entity.PmpProject;
import org.jeecg.modules.project.protree.mapper.PmpProjectMapper;
import org.jeecg.modules.project.protree.service.IPmpProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.io.Serializable;

/**
 * @Description: 项目管理
 * @Author: jeecg-boot
 * @Date:   2020-03-05
 * @Version: V1.0
 */
@Service
public class PmpProjectServiceImpl extends ServiceImpl<PmpProjectMapper, PmpProject> implements IPmpProjectService {
    @Autowired
    PmpProjectMapper pmpProjectMapper;

    @Override
    public void updateMyNode(String projectname, String parentNodeId, String id) {
         pmpProjectMapper.updateMyNode(projectname, parentNodeId, id);
    }

    @Override
    public void isDelteSubNode(String projectname, String id, String isdelete) {
        pmpProjectMapper.isDelteSubNode(projectname, id, isdelete);
    }

    @Override
    public Integer IsSuperior(String id, String principal) {
        return pmpProjectMapper.IsSuperior(id, principal);
    }

    @Override
    public void addSysCategory(PmpProject pmpProject) {
        String categoryCode = "";
        String categoryPid = IPmpProjectService.ROOT_PID_VALUE;
        String parentcode = null;
        if (oConvertUtils.isNotEmpty(pmpProject.getParentnode())) {
            categoryPid = pmpProject.getParentnode();

            //PID 不是根节点 说明需要设置父节点 hasChild 为1
            if (!IPmpProjectService.ROOT_PID_VALUE.equals(categoryPid)) {
                PmpProject parent = baseMapper.selectById(categoryPid);
                parentcode = parent.getProjectcode();
                if (parent != null && !"1".equals(parent.getHaschild())) {
                    parent.setHaschild("1");
                    baseMapper.updateById(parent);
                }
            }
        }
        //update-begin--Author:baihailong  Date:20191209 for：分类字典编码规则生成器做成公用配置
        JSONObject formData = new JSONObject();
        formData.put("parentnode",categoryPid);
        categoryCode = (String) FillRuleUtil.executeRule("task_code_rule",formData);
        pmpProject.setParentnode(categoryPid);
        pmpProject.setProjectcode(categoryCode);
        baseMapper.insert(pmpProject);
    }
}
