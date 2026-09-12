"""
测试报告接口测试
覆盖：报告数据结构、用例统计、Bug分布、通过率计算
"""
import requests


def test_report_structure(base_url, test_project):
    """测试报告返回数据结构完整"""
    resp = requests.get(f"{base_url}/reports/{test_project['id']}")
    data = resp.json()
    assert data["code"] == 200
    report = data["data"]
    assert report["projectId"] == test_project["id"]
    assert "caseStats" in report
    assert "bugSeverityStats" in report
    assert "bugStatusStats" in report


def test_report_empty_project(base_url, test_project):
    """测试空项目报告（无用例无Bug）"""
    resp = requests.get(f"{base_url}/reports/{test_project['id']}")
    report = resp.json()["data"]
    cs = report["caseStats"]
    assert cs["total"] == 0
    assert cs["passed"] == 0
    assert cs["passRate"] == "0.0%"
    assert report["bugSeverityStats"]["total"] == 0


def test_report_case_stats(base_url, test_project):
    """测试报告用例统计数据正确"""
    # 创建需求和用例
    req = requests.post(f"{base_url}/requirements", json={
        "projectId": test_project["id"], "title": "报告测试需求"
    }).json()["data"]
    # 新增3个用例
    case_ids = []
    for i in range(3):
        c = requests.post(f"{base_url}/cases", json={
            "requirementId": req["id"], "title": f"用例{i}", "priority": "P1"
        }).json()["data"]
        case_ids.append(c["id"])
    # 执行：1个通过，1个失败，1个不执行
    requests.post(f"{base_url}/cases/{case_ids[0]}/execute", json={
        "status": "通过", "actualResult": "ok", "executor": "pytest"
    })
    requests.post(f"{base_url}/cases/{case_ids[1]}/execute", json={
        "status": "失败", "actualResult": "fail", "executor": "pytest", "createBug": False
    })

    resp = requests.get(f"{base_url}/reports/{test_project['id']}")
    cs = resp.json()["data"]["caseStats"]
    assert cs["total"] == 3
    assert cs["passed"] == 1
    assert cs["failed"] == 1
    assert cs["pending"] == 1
    # 通过率 = 1/(1+1) = 50%
    assert cs["passRate"] == "50.0%"


def test_report_bug_severity_stats(base_url, test_project):
    """测试报告Bug严重程度分布正确"""
    # 创建不同严重程度的Bug
    for sev in ["致命", "严重", "一般"]:
        requests.post(f"{base_url}/bugs", json={
            "projectId": test_project["id"], "title": f"{sev}Bug", "severity": sev
        })
    resp = requests.get(f"{base_url}/reports/{test_project['id']}")
    bs = resp.json()["data"]["bugSeverityStats"]
    assert bs["致命"] == 1
    assert bs["严重"] == 1
    assert bs["一般"] == 1
    assert bs["total"] == 3


def test_report_not_exist_project(base_url):
    """测试查询不存在项目的报告"""
    resp = requests.get(f"{base_url}/reports/99999")
    # 项目不存在时projectName应为"未知项目"，但不应报错
    assert resp.json()["code"] == 200
