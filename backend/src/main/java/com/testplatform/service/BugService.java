package com.testplatform.service;

import com.testplatform.entity.Bug;
import com.testplatform.mapper.BugMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * Bug Service 层
 * 核心：Bug状态流转校验
 * 状态流转：新建 → 待修复 → 已修复 → 已验证 → 关闭
 */
@Service
public class BugService {

    @Autowired
    private BugMapper bugMapper;

    // 定义合法的状态流转：key=当前状态，value=允许流转到的状态
    private static final Map<String, List<String>> STATUS_FLOW = Map.of(
            "新建",   List.of("待修复", "关闭"),
            "待修复", List.of("已修复", "新建"),
            "已修复", List.of("已验证", "待修复"),  // 已修复可以打回待修复
            "已验证", List.of("关闭", "待修复"),     // 验证不通过可以打回
            "关闭",   List.of("新建")                 // 关闭后可以重新打开
    );

    // 按项目查询Bug列表
    public List<Bug> findByProjectId(Integer projectId) {
        return bugMapper.findByProjectId(projectId);
    }

    // 根据ID查询Bug
    public Bug findById(Integer id) {
        return bugMapper.findById(id);
    }

    // 新增Bug（自动生成Bug编号 BUG-001）
    public Bug add(Bug bug) {
        if (bug.getSeverity() == null || bug.getSeverity().isEmpty()) {
            bug.setSeverity("一般");
        }
        if (bug.getBugPriority() == null || bug.getBugPriority().isEmpty()) {
            bug.setBugPriority("P2");
        }
        if (bug.getStatus() == null || bug.getStatus().isEmpty()) {
            bug.setStatus("新建");
        }
        // 自动生成Bug编号
        List<Bug> existing = bugMapper.findByProjectId(bug.getProjectId());
        int nextNum = existing.size() + 1;
        bug.setBugNo(String.format("BUG-%03d", nextNum));
        bugMapper.insert(bug);
        return bug;
    }

    /**
     * 更新Bug状态（带流转校验）
     * @return 错误信息，null表示成功
     */
    public String updateStatus(Integer id, String newStatus) {
        Bug bug = bugMapper.findById(id);
        if (bug == null) {
            return "Bug不存在";
        }
        // 校验状态流转是否合法
        List<String> allowed = STATUS_FLOW.get(bug.getStatus());
        if (allowed == null || !allowed.contains(newStatus)) {
            return "不允许从【" + bug.getStatus() + "】直接变更为【" + newStatus + "】";
        }
        bugMapper.updateStatus(id, newStatus);
        return null;
    }

    // 更新Bug基本信息（含绑定/解绑用例）
    public boolean update(Bug bug) {
        return bugMapper.update(bug) > 0;
    }

    // 删除Bug
    public boolean delete(Integer id) {
        return bugMapper.deleteById(id) > 0;
    }
}
