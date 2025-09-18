// 添加打卡点管理功能
function loadCheckpoints() {
    fetch('/api/checkpoints')
    .then(response => response.json())
    .then(checkpoints => {
        const tbody = document.querySelector('#checkpointsTable tbody');
        tbody.innerHTML = '';

        checkpoints.forEach(checkpoint => {
            const row = tbody.insertRow();
            row.insertCell(0).textContent = checkpoint.name;
            row.insertCell(1).textContent = checkpoint.location;
            row.insertCell(2).textContent = checkpoint.distance;
            row.insertCell(3).textContent = checkpoint.orderIndex;
            row.insertCell(4).textContent = checkpoint.isStart ? '是' : '否';
            row.insertCell(5).textContent = checkpoint.isFinish ? '是' : '否';
            row.insertCell(6).textContent = checkpoint.isMidpoint ? '是' : '否';

            const actionsCell = row.insertCell(7);
            const editButton = document.createElement('button');
            editButton.textContent = '编辑';
            editButton.onclick = () => editCheckpoint(checkpoint);
            actionsCell.appendChild(editButton);

            const deleteButton = document.createElement('button');
            deleteButton.textContent = '删除';
            deleteButton.onclick = () => deleteCheckpoint(checkpoint.id);
            actionsCell.appendChild(deleteButton);
        });
    });
}

// 添加打卡点表单提交
document.getElementById('checkpointForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const checkpoint = {
        name: document.getElementById('checkpointName').value,
        location: document.getElementById('checkpointLocation').value,
        distance: parseFloat(document.getElementById('checkpointDistance').value),
        orderIndex: parseInt(document.getElementById('checkpointOrder').value),
        isStart: document.getElementById('checkpointIsStart').checked,
        isFinish: document.getElementById('checkpointIsFinish').checked,
        isMidpoint: document.getElementById('checkpointIsMidpoint').checked
    };

    const checkpointId = document.getElementById('checkpointId').value;
    const url = checkpointId ? `/api/checkpoints/${checkpointId}` : '/api/checkpoints';
    const method = checkpointId ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(checkpoint)
    })
    .then(response => response.json())
    .then(data => {
        if (data.id) {
            alert('打卡点保存成功！');
            document.getElementById('checkpointForm').reset();
            document.getElementById('checkpointId').value = '';
            loadCheckpoints();
        } else {
            alert('保存失败: ' + data);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('保存失败');
    });
});

// 编辑打卡点
function editCheckpoint(checkpoint) {
    document.getElementById('checkpointId').value = checkpoint.id;
    document.getElementById('checkpointName').value = checkpoint.name;
    document.getElementById('checkpointLocation').value = checkpoint.location;
    document.getElementById('checkpointDistance').value = checkpoint.distance;
    document.getElementById('checkpointOrder').value = checkpoint.orderIndex;
    document.getElementById('checkpointIsStart').checked = checkpoint.isStart;
    document.getElementById('checkpointIsFinish').checked = checkpoint.isFinish;
    document.getElementById('checkpointIsMidpoint').checked = checkpoint.isMidpoint;
}

// 删除打卡点
function deleteCheckpoint(id) {
    if (confirm('确定要删除这个打卡点吗？')) {
        fetch(`/api/checkpoints/${id}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                alert('删除成功！');
                loadCheckpoints();
            } else {
                alert('删除失败');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('删除失败');
        });
    }
}

// 导出成绩
function exportResults(format) {
    window.open(`/api/results/export?format=${format}`, '_blank');
}

// 发送成绩短信
function sendResultSms(athleteId) {
    fetch(`/api/results/${athleteId}/send-sms`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        alert(data);
    })
    .catch(error => {
        console.error('Error:', error);
        alert('发送短信失败');
    });
}

// 在页面加载时初始化所有数据
document.addEventListener('DOMContentLoaded', function() {
    loadAthletes();
    loadResults();
    loadCheckpoints();
});