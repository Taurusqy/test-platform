"""
Bug缺陷接口测试
覆盖：新增、查询、状态流转（合法+非法）、编辑绑定用例、删除
"""
import requests


def _add_bug(base_url, project_id, title="测试Bug", severity="一般"):
    """辅助：新增Bug"""
    resp = requests.post(f"{base_url}/bugs", json={
        "projectId": project_id,
        "title": title,
        "description": "Bug描述",
        "severity": severity,
        "reporter": "pytest"
    })
    return resp.json()["data"]


def test_add_bug_success(base_url, test_project):
    """测试新增Bug成功"""
    bug = _add_bug(base_url, test_project["id"])
    assert bug["id"] is not None
    assert bug["status"] == "新建"  # 默认状态
    assert bug["severity"] == "一般"


def test_add_bug_no_project(base_url):
    """测试新增Bug不指定项目，应报错"""
    resp = requests.post(f"{base_url}/bugs", json={"title": "无项目Bug"})
    assert resp.json()["code"] == 500


def test_get_bugs_by_project(base_url, test_project):
    """测试按项目查询Bug列表"""
    _add_bug(base_url, test_project["id"], "BugA")
    _add_bug(base_url, test_project["id"], "BugB")
    resp = requests.get(f"{base_url}/bugs", params={"projectId": test_project["id"]})
    assert resp.json()["code"] == 200
    assert len(resp.json()["data"]) >= 2


def test_bug_status_normal_flow(base_url, test_project):
    """测试Bug状态正常流转：新建→待修复→已修复→已验证→关闭"""
    bug = _add_bug(base_url, test_project["id"])
    flow = ["待修复", "已修复", "已验证", "关闭"]
    for status in flow:
        resp = requests.put(f"{base_url}/bugs/{bug['id']}/status", json={"status": status})
        assert resp.json()["code"] == 200, f"流转到{status}失败"
    # 验证最终状态
    get_resp = requests.get(f"{base_url}/bugs/{bug['id']}")
    assert get_resp.json()["data"]["status"] == "关闭"


def test_bug_status_invalid_flow(base_url, test_project):
    """测试Bug状态非法流转：新建→已验证，应报错"""
    bug = _add_bug(base_url, test_project["id"])
    resp = requests.put(f"{base_url}/bugs/{bug['id']}/status", json={"status": "已验证"})
    assert resp.json()["code"] == 500
    assert "不允许" in resp.json()["message"]


def test_bug_status_reopen(base_url, test_project):
    """测试Bug关闭后重新打开"""
    bug = _add_bug(base_url, test_project["id"])
    # 先流转到关闭
    for s in ["待修复", "已修复", "已验证", "关闭"]:
        requests.put(f"{base_url}/bugs/{bug['id']}/status", json={"status": s})
    # 重新打开
    resp = requests.put(f"{base_url}/bugs/{bug['id']}/status", json={"status": "新建"})
    assert resp.json()["code"] == 200
    get_resp = requests.get(f"{base_url}/bugs/{bug['id']}")
    assert get_resp.json()["data"]["status"] == "新建"


def test_bug_status_fixed_rejected(base_url, test_project):
    """测试已修复打回待修复"""
    bug = _add_bug(base_url, test_project["id"])
    for s in ["待修复", "已修复"]:
        requests.put(f"{base_url}/bugs/{bug['id']}/status", json={"status": s})
    resp = requests.put(f"{base_url}/bugs/{bug['id']}/status", json={"status": "待修复"})
    assert resp.json()["code"] == 200


def test_update_bug_bind_case(base_url, test_project):
    """测试编辑Bug并绑定用例"""
    bug = _add_bug(base_url, test_project["id"])
    resp = requests.put(f"{base_url}/bugs/{bug['id']}", json={
        "title": "更新后的Bug",
        "description": "更新描述",
        "severity": "严重",
        "assignee": "开发A",
        "caseId": 1
    })
    assert resp.json()["code"] == 200
    get_resp = requests.get(f"{base_url}/bugs/{bug['id']}")
    assert get_resp.json()["data"]["caseId"] == 1
    assert get_resp.json()["data"]["assignee"] == "开发A"


def test_delete_bug(base_url, test_project):
    """测试删除Bug"""
    bug = _add_bug(base_url, test_project["id"])
    del_resp = requests.delete(f"{base_url}/bugs/{bug['id']}")
    assert del_resp.json()["code"] == 200
    get_resp = requests.get(f"{base_url}/bugs/{bug['id']}")
    assert get_resp.json()["code"] == 500
