#!/usr/bin/env python3
from flask import Flask, jsonify, request, Blueprint

app = Flask(__name__)

# 登录
# mcs回复"code":	"0", 就是能登陆。账户校验都在mcs中做
@app.route('/api/pdaToMcs/login', methods=['POST'])
def login():
    req_data = request.get_json()
    print("login：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"login success",
        "data": {
            "username":	    "admin",
            "nickName":	    "admin",
            "deptId":		"admin",
            "employeeId":	"admin",
        }
    }

    return jsonify(response)


# 任务查询
@app.route('/api/pdaToMcs/taskQuery', methods=['POST'])
def task_query():
    req_data = request.get_json()
    print("taskQuery：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"Task query successful",
        "data": [
            {
                "taskCode":	    "123",
                "status":	    "1",
                "stage":		"2",
                "startCell":	"3",
                "targetCell":	"4",
                "carCode":	    "5",
                "agvId":	    "6",
            },
            {
                "taskCode":	    "456",
                "status":	    "1",
                "stage":		"2",
                "startCell":	"3",
                "targetCell":	"4",
                "carCode":	    "5",
                "agvId":	    "6",
            }
        ]
    }

    return jsonify(response)


# 取消任务
@app.route('/api/pdaToMcs/taskCancel', methods=['POST'])
def task_cancel():
    req_data = request.get_json()
    print("taskCancel：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"Task canceled successfully",
    }

    return jsonify(response)

# 获取所有错误信息
@app.route('/api/pdaToMcs/fetchErrorMsg', methods=['POST'])
def fetch_error_msg():
    req_data = request.get_json()
    print("fetchErrorMsg：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"success",
        "data": [
            {
                "msgID":	    "1",
                "errorID":	    "2",
                "errorDeviceID":		"3",
                "errorTime": "2025/10/08",
                "errorMsg": {
                    "errorSource": "4",
                    "errorMessage": "5",
                    "errorLevel": "6",
                    "recoverySteps": "7",
                },
            },
            {
                "msgID":	    "11",
                "errorID":	    "22",
                "errorDeviceID":		"33",
                "errorTime": "2025/10/08",
                "errorMsg": {
                    "errorSource": "44",
                    "errorMessage": "55",
                    "errorLevel": "66",
                    "recoverySteps": "77",
                },
            }
        ]
    }

    return jsonify(response)


# 异常清除
@app.route('/api/pdaToMcs/notifyErrorClear', methods=['POST'])
def notify_error_clear():
    req_data = request.get_json()
    print("notifyErrorClear：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"Clear error successfully",
    }

    return jsonify(response)


# 根据产品id，查询绑定的料车id，和该料车所绑定的地码
@app.route('/api/pdaToMcs/queryProductStation', methods=['POST'])
def query_product_station():
    req_data = request.get_json()
    print("queryProductStation：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"Clear error successfully",
        "station": "ProductSN: ? is Bound to cartCode: ? GroudCode: ?",
    }

    return jsonify(response)

# 根据地码，查询绑定的料车
@app.route('/api/pdaToMcs/queryGroundBindingInfo', methods=['POST'])
def query_ground_binding_info():
    req_data = request.get_json()
    print("queryGroundBindingInfo：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"Query ground binding info successfully",
        "station": "cart: ? 绑定了地码: ?",
    }

    return jsonify(response)

# 把产品绑定到该料车上，料车也绑定到该地码上
@app.route('/api/pdaToMcs/bindCartPosition', methods=['POST'])
def bind_cart_position():
    req_data = request.get_json()
    print("bindCartPosition：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"bind CartPosition successfully",
    }

    return jsonify(response)


# 根据：地码、料车ID、产品ID（没用到），发请求。生成AGV任务
@app.route('/api/pdaToMcs/sendTrolleyByAGV', methods=['POST'])
def send_trolley_by_agv():
    req_data = request.get_json()
    print("sendTrolleyByAGV：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"Task created successfully",
    }

    return jsonify(response)


# 把产品绑定到该料车上，料车也绑定到该地码上
@app.route('/api/pdaToMcs/bindCartProducts', methods=['POST'])
def bind_cart_products():
    req_data = request.get_json()
    print("bindCartProducts：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"bind CartProducts successfully",
    }

    return jsonify(response)


# 查询该料车绑定的地码、产品
@app.route('/api/pdaToMcs/queryCartBindingInfo', methods=['POST'])
def query_cart_binding_info():
    req_data = request.get_json()
    print("queryCartBindingInfo：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"Query cart binding info successfully",
        "data": {
            "groundCode": "11",
            "cartProducts": [
                {
                    "productSN": "1111",
                    "position": "1",
                },
                {
                    "productSN": "2222",
                    "position": "2",
                },
                {
                    "productSN": "3333",
                    "position": "3",
                }
            ]
        }
    }

    return jsonify(response)


# 根据：料车ID，解绑该料车的地码为空字符串
@app.route('/api/pdaToMcs/unbindCartPosition', methods=['POST'])
def unbind_cart_position():
    req_data = request.get_json()
    print("unbindCartPosition：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"unbind CartPosition successfully",
    }

    return jsonify(response)

# 根据：料车ID，解绑该料车的产品为空字符串
@app.route('/api/pdaToMcs/unbindCartProducts', methods=['POST'])
def unbind_cart_products():
    req_data = request.get_json()
    print("unbindCartProducts：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"unbind CartProducts successfully",
    }

    return jsonify(response)

# 在该地码上，绑定一个叉车产品SN
@app.route('/api/pdaToMcs/bindForkPosition', methods=['POST'])
def bind_fork_position():
    req_data = request.get_json()
    print("bindForkPosition：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"unbind ForkliftProduct successfully",
    }

    return jsonify(response)


# 在该地码上，解绑一个叉车产品SN
@app.route('/api/pdaToMcs/unbindForkPosition', methods=['POST'])
def unbind_fork_position():
    req_data = request.get_json()
    print("unbindForkPosition：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"unbind ForkliftProduct successfully",
    }

    return jsonify(response)


# 在该地码上，解绑一个叉车产品SN
@app.route('/api/pdaToMcs/queryFlProductBindingInfo', methods=['POST'])
def query_fl_product_binding_info():
    req_data = request.get_json()
    print("queryFlProductBindingInfo：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"Query fl product binding info successfully",
        "data": {
            "cellID": "1",
            "flProduct": "123"
        }
    }

    return jsonify(response)


# 抓取库位，用于让AGV拉来该料车
@app.route('/api/pdaToMcs/callfindStorage', methods=['POST'])
def call_find_storage():
    req_data = request.get_json()
    print("callfindStorage：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"3,E1,E2,E3",
    }

    return jsonify(response)


# 抓取地码，用于让AGV拉来该料车
@app.route('/api/pdaToMcs/callfindLocation', methods=['POST'])
def call_find_location_or_Box_Size():
    req_data = request.get_json()
    print("callfindLocation：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"5,1,2,3,4,5,boxSize",
    }

    return jsonify(response)


# 让AGV拉来该料车
@app.route('/api/pdaToMcs/callSelected', methods=['POST'])
def call_selected():
    req_data = request.get_json()
    print("callSelected：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"Task created successfully",
    }

    return jsonify(response)


# 抓取库位，用于让AGV拖走该料车
@app.route('/api/pdaToMcs/sendfetchStorage', methods=['POST'])
def send_fetch_storage():
    req_data = request.get_json()
    print("sendfetchStorage：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"3,E11,E22,E33",
    }

    return jsonify(response)

# 抓取地码，用于让AGV拖走该料车
@app.route('/api/pdaToMcs/sendfetchLocation', methods=['POST'])
def send_fetch_location():
    req_data = request.get_json()
    print("sendfetchLocation：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"3,位置11,位置22,位置33",
    }

    return jsonify(response)


# 让AGV拖走该料车
@app.route('/api/pdaToMcs/sendSelected', methods=['POST'])
def send_selected():
    req_data = request.get_json()
    print("sendSelected：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"Task created successfully",
    }

    return jsonify(response)


#################
# 抓取库位，用于让AGF拉来该料车
@app.route('/api/pdaToMcs/callfindStorageAGF', methods=['POST'])
def call_find_storage_agf():
    req_data = request.get_json()
    print("callfindStorageAGF：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"3,E1,E2,E3",
    }

    return jsonify(response)


# 抓取地码，用于让AGF拉来该料车
@app.route('/api/pdaToMcs/callfindLocationAGF', methods=['POST'])
def call_find_location_agf():
    req_data = request.get_json()
    print("callfindLocationAGF：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"3,位置1,位置2,位置3",
    }

    return jsonify(response)


# 让AGF来该料车
@app.route('/api/pdaToMcs/callSelectedAGF', methods=['POST'])
def call_selected_agf():
    req_data = request.get_json()
    print("callSelectedAGF：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"Task created successfully",
    }

    return jsonify(response)


# 抓取库位，用于让AGF拖走该料车
@app.route('/api/pdaToMcs/sendfetchStorageAGF', methods=['POST'])
def send_fetch_storage_agf():
    req_data = request.get_json()
    print("sendfetchStorageAGF：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"3,E11,E22,E33",
    }

    return jsonify(response)

# 抓取地码，用于让AGF拖走该料车
@app.route('/api/pdaToMcs/sendfetchLocationAGF', methods=['POST'])
def send_fetch_location_agf():
    req_data = request.get_json()
    print("sendfetchLocationAGF：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"3,位置11,位置22,位置33",
    }

    return jsonify(response)


# 让AGF拖走该料车
@app.route('/api/pdaToMcs/sendSelectedAGF', methods=['POST'])
def send_selected_agf():
    req_data = request.get_json()
    print("sendSelectedAGF：" + str(req_data))

    response = {
        "code":	"0",
        "message":	"Task created successfully",
    }

    return jsonify(response)


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=9090)
