"""
需求管理接口测试
覆盖：新增、按项目查询、查询单个、删除、参数校验
"""
import requests


def _add_requirement(base_url, project_id, title="测试需求"):
    """辅助方法：新增需求"""
    resp = requests.post(f"{base_url}/requirements", json={
        "projectId": project_id,
        "title": title,
        "content": "测试需求内容",
        "priority": "高"
    })
    return resp.json()["data"]


def test_add_requirement_success(base_url, test_project):
    """测试新增需求成功"""
    req = _add_requirement(base_url, test_project["id"])
    assert req["id"] is not None
    assert req["projectId"] == test_project["id"]
    assert req["status"] == "待分析"  # 默认状态


def test_add_requirement_no_project(base_url):
    """测试新增需求不指定项目ID，应报错"""
    resp = requests.post(f"{base_url}/requirements", json={
        "title": "无项目的需求"
    })
    assert resp.json()["code"] == 500


def test_add_requirement_title_empty(base_url, test_project):
    """测试新增需求标题为空，应报错"""
    resp = requests.post(f"{base_url}/requirements", json={
        "projectId": test_project["id"],
        "title": ""
    })
    assert resp.json()["code"] == 500


def test_get_requirements_by_project(base_url, test_project):
    """测试按项目查询需求列表"""
    _add_requirement(base_url, test_project["id"], "需求A")
    _add_requirement(base_url, test_project["id"], "需求B")
    resp = requests.get(f"{base_url}/requirements", params={"projectId": test_project["id"]})
    data = resp.json()
    assert data["code"] == 200
    assert len(data["data"]) >= 2


def test_get_requirement_by_id(base_url, test_project):
    """测试根据ID查询单个需求"""
    req = _add_requirement(base_url, test_project["id"])
    resp = requests.get(f"{base_url}/requirements/{req['id']}")
    assert resp.json()["code"] == 200
    assert resp.json()["data"]["title"] == req["title"]


def test_delete_requirement(base_url, test_project):
    """测试删除需求"""
    req = _add_requirement(base_url, test_project["id"])
    del_resp = requests.delete(f"{base_url}/requirements/{req['id']}")
    assert del_resp.json()["code"] == 200
    # 验证已删除
    get_resp = requests.get(f"{base_url}/requirements/{req['id']}")
    assert get_resp.json()["code"] == 500


def test_requirement_isolation_between_projects(base_url, test_project):
    """测试需求隔离：项目A的需求不应出现在项目B"""
    # 创建另一个项目
    proj2 = requests.post(f"{base_url}/projects", json={"name": "隔离测试项目B"}).json()["data"]
    try:
        _add_requirement(base_url, test_project["id"], "项目A的需求")
        # 查询项目B的需求，不应包含项目A的
        resp = requests.get(f"{base_url}/requirements", params={"projectId": proj2["id"]})
        titles = [r["title"] for r in resp.json()["data"]]
        assert "项目A的需求" not in titles
    finally:
        requests.delete(f"{base_url}/projects/{proj2['id']}")
