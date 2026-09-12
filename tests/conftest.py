"""
pytest 公共配置
提供 base_url 和测试项目的自动创建/清理
"""
import pytest
import requests

BASE_URL = "http://localhost:8080/api"


@pytest.fixture(scope="session")
def base_url():
    """返回接口基础地址"""
    return BASE_URL


@pytest.fixture(scope="function")
def test_project(base_url):
    """
    每个测试函数自动创建一个测试项目，测试结束后自动删除
    返回项目字典，包含 id
    """
    # 前置：创建测试项目
    resp = requests.post(f"{base_url}/projects", json={
        "name": "pytest自动化测试项目",
        "description": "由pytest自动创建，测试完成后自动删除",
        "status": "进行中"
    })
    project = resp.json()["data"]
    yield project
    # 后置：删除测试项目（级联删除其下所有需求、用例、Bug）
    requests.delete(f"{base_url}/projects/{project['id']}")
