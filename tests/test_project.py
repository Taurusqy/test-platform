"""
项目管理接口测试
覆盖：新增、查询列表、查询单个、更新、删除、参数校验
"""
import requests


def test_add_project_success(base_url):
    """测试新增项目成功"""
    resp = requests.post(f"{base_url}/projects", json={
        "name": "pytest新增项目",
        "description": "测试描述",
        "status": "进行中"
    })
    assert resp.status_code == 200
    data = resp.json()
    assert data["code"] == 200
    assert data["data"]["name"] == "pytest新增项目"
    assert data["data"]["id"] is not None
    # 清理
    requests.delete(f"{base_url}/projects/{data['data']['id']}")


def test_add_project_name_empty(base_url):
    """测试新增项目名称为空，应返回错误"""
    resp = requests.post(f"{base_url}/projects", json={
        "name": "",
        "description": "测试"
    })
    data = resp.json()
    assert data["code"] == 500
    assert "名称" in data["message"]


def test_get_project_list(base_url, test_project):
    """测试查询项目列表"""
    resp = requests.get(f"{base_url}/projects")
    data = resp.json()
    assert data["code"] == 200
    assert isinstance(data["data"], list)
    # 列表中应包含刚创建的测试项目
    names = [p["name"] for p in data["data"]]
    assert test_project["name"] in names


def test_get_project_by_id(base_url, test_project):
    """测试根据ID查询单个项目"""
    resp = requests.get(f"{base_url}/projects/{test_project['id']}")
    data = resp.json()
    assert data["code"] == 200
    assert data["data"]["id"] == test_project["id"]


def test_get_project_not_exist(base_url):
    """测试查询不存在的项目"""
    resp = requests.get(f"{base_url}/projects/99999")
    data = resp.json()
    assert data["code"] == 500


def test_update_project(base_url, test_project):
    """测试更新项目"""
    resp = requests.put(f"{base_url}/projects/{test_project['id']}", json={
        "name": "更新后的项目名",
        "description": "更新后的描述",
        "status": "已完成"
    })
    assert resp.json()["code"] == 200
    # 验证更新生效
    get_resp = requests.get(f"{base_url}/projects/{test_project['id']}")
    assert get_resp.json()["data"]["name"] == "更新后的项目名"
    assert get_resp.json()["data"]["status"] == "已完成"


def test_delete_project(base_url):
    """测试删除项目"""
    # 先创建
    create_resp = requests.post(f"{base_url}/projects", json={"name": "待删除项目"})
    pid = create_resp.json()["data"]["id"]
    # 删除
    del_resp = requests.delete(f"{base_url}/projects/{pid}")
    assert del_resp.json()["code"] == 200
    # 验证已删除
    get_resp = requests.get(f"{base_url}/projects/{pid}")
    assert get_resp.json()["code"] == 500
