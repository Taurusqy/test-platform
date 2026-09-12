"""
测试用例接口测试
覆盖：新增、编辑、删除、执行（通过/失败/阻塞）、失败联动建Bug、参数校验
"""
import requests


def _add_requirement(base_url, project_id):
    """辅助：新增需求，返回需求ID"""
    resp = requests.post(f"{base_url}/requirements", json={
        "projectId": project_id, "title": "用例测试需求", "priority": "高"
    })
    return resp.json()["data"]["id"]


def _add_case(base_url, requirement_id, title="测试用例"):
    """辅助：新增用例，返回用例字典"""
    resp = requests.post(f"{base_url}/cases", json={
        "requirementId": requirement_id,
        "title": title,
        "precondition": "前置条件",
        "steps": "步骤1\n步骤2",
        "expectedResult": "预期结果",
        "priority": "P0"
    })
    return resp.json()["data"]


def test_add_case_success(base_url, test_project):
    """测试新增用例成功"""
    req_id = _add_requirement(base_url, test_project["id"])
    case = _add_case(base_url, req_id)
    assert case["id"] is not None
    assert case["status"] == "未执行"  # 默认状态
    assert case["priority"] == "P0"
    assert case["caseNo"] is not None  # 自动生成用例编号


def test_add_case_no_requirement(base_url):
    """测试新增用例不指定需求ID，应报错"""
    resp = requests.post(f"{base_url}/cases", json={"title": "无需求用例"})
    assert resp.json()["code"] == 500


def test_update_case(base_url, test_project):
    """测试编辑用例"""
    req_id = _add_requirement(base_url, test_project["id"])
    case = _add_case(base_url, req_id)
    resp = requests.put(f"{base_url}/cases/{case['id']}", json={
        "title": "编辑后的用例",
        "precondition": "新前置",
        "steps": "新步骤",
        "expectedResult": "新预期",
        "priority": "P1"
    })
    assert resp.json()["code"] == 200
    # 验证
    get_resp = requests.get(f"{base_url}/cases/{case['id']}")
    assert get_resp.json()["data"]["title"] == "编辑后的用例"


def test_delete_case(base_url, test_project):
    """测试删除用例"""
    req_id = _add_requirement(base_url, test_project["id"])
    case = _add_case(base_url, req_id)
    del_resp = requests.delete(f"{base_url}/cases/{case['id']}")
    assert del_resp.json()["code"] == 200
    get_resp = requests.get(f"{base_url}/cases/{case['id']}")
    assert get_resp.json()["code"] == 500


def test_execute_case_pass(base_url, test_project):
    """测试执行用例-通过"""
    req_id = _add_requirement(base_url, test_project["id"])
    case = _add_case(base_url, req_id)
    resp = requests.post(f"{base_url}/cases/{case['id']}/execute", json={
        "status": "通过",
        "actualResult": "执行通过",
        "executor": "pytest"
    })
    assert resp.json()["code"] == 200
    assert resp.json()["data"] is None  # 通过不创建Bug
    # 验证状态更新
    get_resp = requests.get(f"{base_url}/cases/{case['id']}")
    assert get_resp.json()["data"]["status"] == "通过"


def test_execute_case_fail_no_bug(base_url, test_project):
    """测试执行用例-失败但不建Bug"""
    req_id = _add_requirement(base_url, test_project["id"])
    case = _add_case(base_url, req_id)
    resp = requests.post(f"{base_url}/cases/{case['id']}/execute", json={
        "status": "失败",
        "actualResult": "功能异常",
        "executor": "pytest",
        "createBug": False
    })
    assert resp.json()["code"] == 200
    assert resp.json()["data"] is None


def test_execute_case_fail_with_bug(base_url, test_project):
    """测试执行用例-失败并联动创建Bug"""
    req_id = _add_requirement(base_url, test_project["id"])
    case = _add_case(base_url, req_id)
    resp = requests.post(f"{base_url}/cases/{case['id']}/execute", json={
        "status": "失败",
        "actualResult": "登录后白屏",
        "executor": "pytest",
        "createBug": True,
        "bugTitle": "登录白屏Bug",
        "bugSeverity": "严重"
    })
    data = resp.json()
    assert data["code"] == 200
    bug = data["data"]
    assert bug is not None
    assert bug["id"] is not None
    assert bug["caseId"] == case["id"]
    assert bug["title"] == "登录白屏Bug"
    assert bug["severity"] == "严重"
    assert bug["status"] == "新建"
    assert bug["projectId"] == test_project["id"]


def test_execute_case_blocked(base_url, test_project):
    """测试执行用例-阻塞"""
    req_id = _add_requirement(base_url, test_project["id"])
    case = _add_case(base_url, req_id)
    resp = requests.post(f"{base_url}/cases/{case['id']}/execute", json={
        "status": "阻塞", "actualResult": "环境不可用", "executor": "pytest"
    })
    assert resp.json()["code"] == 200
    get_resp = requests.get(f"{base_url}/cases/{case['id']}")
    assert get_resp.json()["data"]["status"] == "阻塞"


def test_execute_case_invalid_status(base_url, test_project):
    """测试执行用例-非法状态值，应报错"""
    req_id = _add_requirement(base_url, test_project["id"])
    case = _add_case(base_url, req_id)
    resp = requests.post(f"{base_url}/cases/{case['id']}/execute", json={
        "status": "未知状态"
    })
    assert resp.json()["code"] == 500


def test_execute_case_not_exist(base_url):
    """测试执行不存在的用例，应报错"""
    resp = requests.post(f"{base_url}/cases/99999/execute", json={"status": "通过"})
    assert resp.json()["code"] == 500
