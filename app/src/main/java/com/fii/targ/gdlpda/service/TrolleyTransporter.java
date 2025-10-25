package com.fii.targ.gdlpda.service;

import com.fii.targ.gdlpda.model.AutoTransportResponseBody;
import com.fii.targ.gdlpda.model.CartInfo;
import com.fii.targ.gdlpda.model.FetchAvailableTrolleyResponse;
import com.fii.targ.gdlpda.model.TolleryTransportResponseBody;
import com.fii.targ.gdlpda.model.locationResponseBody;
import com.fii.targ.gdlpda.model.storageResponseBody;
import com.fii.targ.gdlpda.ui.TrolleyInfo;
import com.fii.targ.gdlpda.conn.HttpClient;
import com.fii.targ.gdlpda.conn.ApiResponse;

import java.util.ArrayList;
import java.util.List;

public class TrolleyTransporter {

    public static boolean call(String cellId, String cartCode) {
        try {
            TolleryTransportRequestBody requestBody = new TolleryTransportRequestBody(cellId, cartCode);
            ApiResponse<TolleryTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.CALL_TROLLEY, requestBody, TolleryTransportResponseBody.class);
            return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean call(String cellId) {
        try {
            TolleryTransportRequestBody requestBody = new TolleryTransportRequestBody(cellId);
            ApiResponse<TolleryTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.CALL_TROLLEY, requestBody, TolleryTransportResponseBody.class);
            return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static TolleryTransportResponseBody callMold(String cellId,String agvType) {
        try {
            TolleryTransportCallMoldRequestBody requestBody = new TolleryTransportCallMoldRequestBody(cellId,agvType);
            ApiResponse<TolleryTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.CALL_TROLLEY, requestBody, TolleryTransportResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            TolleryTransportResponseBody errorResponse = new TolleryTransportResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static TolleryTransportResponseBody send(String cartCode) {
        try {
            TolleryTransportSendRequestBody requestBody = new TolleryTransportSendRequestBody(cartCode);
            ApiResponse<TolleryTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.SEND_TROLLEY, requestBody, TolleryTransportResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            TolleryTransportResponseBody errorResponse = new TolleryTransportResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static TolleryTransportResponseBody sendMold(String cartCode,String agvType) {
        try {
            TolleryTransportSendMoldRequestBody requestBody = new TolleryTransportSendMoldRequestBody(cartCode,agvType);
            ApiResponse<TolleryTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.SEND_TROLLEY, requestBody, TolleryTransportResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            TolleryTransportResponseBody errorResponse = new TolleryTransportResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static TolleryTransportResponseBody sendSelected(String cartCode,String agvType,String storageCode,String location) {
        try {
            TolleryTransportSendSelectRequestBody requestBody = new TolleryTransportSendSelectRequestBody(cartCode,agvType,storageCode,location);
            ApiResponse<TolleryTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.SEND_SELECTED, requestBody, TolleryTransportResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            TolleryTransportResponseBody errorResponse = new TolleryTransportResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static TolleryTransportResponseBody sendSelectedAGF(String cellId,String agvType,String storageCode,String location,String height) {
        try {
            TolleryTransportSendSelectAGFRequestBody requestBody = new TolleryTransportSendSelectAGFRequestBody(cellId,agvType,storageCode,location,height);
            ApiResponse<TolleryTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.SEND_SELECTED_AGF, requestBody, TolleryTransportResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            TolleryTransportResponseBody errorResponse = new TolleryTransportResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static TolleryTransportResponseBody callSelected(String cellId,String agvType,String storageCode,String location, String boxSize) {
        try {
            TolleryTransportCallSelectRequestBody requestBody = new TolleryTransportCallSelectRequestBody(cellId,agvType,storageCode,location, boxSize);
            ApiResponse<TolleryTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.CALL_SELECTED, requestBody, TolleryTransportResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            TolleryTransportResponseBody errorResponse = new TolleryTransportResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static TolleryTransportResponseBody callSelectedAGF(String cellId,String agvType,String storageCode,String location,String height) {
        try {
            TolleryTransportCallSelectAGFRequestBody requestBody = new TolleryTransportCallSelectAGFRequestBody(cellId,agvType,storageCode,location,height);
            ApiResponse<TolleryTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.CALL_SELECTED_AGF, requestBody, TolleryTransportResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            TolleryTransportResponseBody errorResponse = new TolleryTransportResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static AutoTransportResponseBody sendAGV(String cartCode, String cellId, String productCode, String agvType) {
        try {
            AutoTransportSendRequestBody requestBody = new AutoTransportSendRequestBody(cartCode, cellId, productCode, agvType);
            ApiResponse<AutoTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.SEND_TROLLEY, requestBody, AutoTransportResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            AutoTransportResponseBody errorResponse = new AutoTransportResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static AutoTransportResponseBody sendAGF(String cartCode, String cellId, String productCode, String agvType) {
        try {
            AutoTransportSendRequestBody requestBody = new AutoTransportSendRequestBody(cartCode, cellId, productCode, agvType);
            ApiResponse<AutoTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.SEND_Fl_TROLLEY, requestBody, AutoTransportResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            AutoTransportResponseBody errorResponse = new AutoTransportResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static FetchAvailableTrolleyResponse fetchAvailableTrolleys(String cellId) {
        try {
            FetchAvailableTrolleyRequestBody requestBody = new FetchAvailableTrolleyRequestBody(cellId);
            ApiResponse<FetchAvailableTrolleyResponse> response = HttpClient.post(Constants.getBaseUrl() + Constants.FETCH_AVAILABLE_TROLLEY, requestBody, FetchAvailableTrolleyResponse.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            FetchAvailableTrolleyResponse errorResponse = new FetchAvailableTrolleyResponse();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static List<TrolleyInfo> convertToTrolleyInfoList(List<CartInfo> cartInfos) {
        List<TrolleyInfo> trolleyInfos = new ArrayList<>();
        if (cartInfos == null) return trolleyInfos;
        for (CartInfo cartInfo : cartInfos) {
            trolleyInfos.add(new TrolleyInfo(cartInfo.getCartCode(), cartInfo.getCellId()));
        }
        return trolleyInfos;
    }

    public static storageResponseBody callFindStorage(String cellId, String agvType) {
        try {
            callStorageRequestBody requestBody = new callStorageRequestBody(cellId,agvType);
            ApiResponse<storageResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.CALL_FIND_STORAGE, requestBody, storageResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            storageResponseBody errorResponse = new storageResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static storageResponseBody callFindStorageAGF(String cellId, String agvType) {
        try {
            callStorageRequestBody requestBody = new callStorageRequestBody(cellId,agvType);
            ApiResponse<storageResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.CALL_FIND_STORAGE_AGF, requestBody, storageResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            storageResponseBody errorResponse = new storageResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static locationResponseBody callFindLocation(String cellId, String agvType, String storageCode) {
        try {
            callLocationRequestBody requestBody = new callLocationRequestBody(cellId,agvType,storageCode);
            ApiResponse<locationResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.CALL_FIND_LOCATION, requestBody, locationResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            locationResponseBody errorResponse = new locationResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static locationResponseBody callFindLocationAGF(String cellId, String agvType, String storageCode) {
        try {
            callLocationRequestBody requestBody = new callLocationRequestBody(cellId,agvType,storageCode);
            ApiResponse<locationResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.CALL_FIND_LOCATION_AGF, requestBody, locationResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            locationResponseBody errorResponse = new locationResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static storageResponseBody sendFetchStorage(String cartCode, String agvType) {
        try {
            sendStorageRequestBody requestBody = new sendStorageRequestBody(cartCode,agvType);
            ApiResponse<storageResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.SEND_FETCH_STORAGE, requestBody, storageResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            storageResponseBody errorResponse = new storageResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static storageResponseBody sendFetchStorageAGF(String cellId, String agvType) {
        try {
            sendStorageAGFRequestBody requestBody = new sendStorageAGFRequestBody(cellId,agvType);
            ApiResponse<storageResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.SEND_FETCH_STORAGE_AGF, requestBody, storageResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            storageResponseBody errorResponse = new storageResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static locationResponseBody sendFetchLocation(String cartCode, String agvType, String storageCode) {
        try {
            sendLocationRequestBody requestBody = new sendLocationRequestBody(cartCode,agvType,storageCode);
            ApiResponse<locationResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.SEND_FETCH_LOCATION, requestBody, locationResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            locationResponseBody errorResponse = new locationResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static locationResponseBody sendFetchLocationAGF(String cellId, String agvType, String storageCode) {
        try {
            sendLocationAGFRequestBody requestBody = new sendLocationAGFRequestBody(cellId,agvType,storageCode);
            ApiResponse<locationResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.SEND_FETCH_LOCATION_AGF, requestBody, locationResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            locationResponseBody errorResponse = new locationResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }

    public static TolleryTransportResponseBody actionrequest(String cellId, String agvType, String action) {
        try {
            ActionRequestBody requestBody = new ActionRequestBody(cellId,agvType,action);
            ApiResponse<TolleryTransportResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.ACTION_REQUEST, requestBody, TolleryTransportResponseBody.class);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            TolleryTransportResponseBody errorResponse = new TolleryTransportResponseBody();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return errorResponse;
        }
    }
}

// 添加新的请求体类
class ActionRequestBody {
    private String cellId;
    private String agvType;
    private String action;

    public ActionRequestBody(String cellId, String agvType, String action) {
        this.cellId = cellId;
        this.agvType = agvType;
        this.action = action;
    }
}

class sendStorageRequestBody {
    private String cartCode;
    private String agvType;

    public sendStorageRequestBody(String cartCode, String agvType) {
        this.cartCode = cartCode;
        this.agvType = agvType;
    }
}

class sendLocationRequestBody {
    private String cartCode;
    private String agvType;
    private String storageCode;

    public sendLocationRequestBody(String cartCode, String agvType, String storageCode) {
        this.cartCode = cartCode;
        this.agvType = agvType;
        this.storageCode = storageCode;
    }
}
class callStorageRequestBody {
    private String cellId;
    private String agvType;

    public callStorageRequestBody(String cellId, String agvType) {
        this.cellId = cellId;
        this.agvType = agvType;
    }
}

class callLocationRequestBody {
    private String cellId;
    private String agvType;
    private String storageCode;

    public callLocationRequestBody(String cellId, String agvType, String storageCode) {
        this.cellId = cellId;
        this.agvType = agvType;
        this.storageCode = storageCode;
    }
}
class sendStorageAGFRequestBody {
    private String cellId;
    private String agvType;

    public sendStorageAGFRequestBody(String cellId, String agvType) {
        this.cellId = cellId;
        this.agvType = agvType;
    }
}

class sendLocationAGFRequestBody {
    private String cellId;
    private String agvType;
    private String storageCode;

    public sendLocationAGFRequestBody(String cellId, String agvType, String storageCode) {
        this.cellId = cellId;
        this.agvType = agvType;
        this.storageCode = storageCode;
    }
}

class TolleryTransportSendRequestBody {
    private String cellId;

    public TolleryTransportSendRequestBody(String cellId) {
        this.cellId = cellId;
    }
}

class TolleryTransportSendMoldRequestBody {
    private String cartCode;
    private String agvType;

    public TolleryTransportSendMoldRequestBody(String cartCode, String agvType) {
        this.cartCode = cartCode;
        this.agvType = agvType;
    }
}

class TolleryTransportSendSelectRequestBody {
    private String cartCode;
    private String agvType;
    private String storageCode;
    private String location;

    public TolleryTransportSendSelectRequestBody(String cartCode, String agvType, String storageCode, String location) {
        this.cartCode = cartCode;
        this.agvType = agvType;
        this.storageCode = storageCode;
        this.location = location;
    }
}

class TolleryTransportCallSelectRequestBody {
    private String cellId;
    private String agvType;
    private String storageCode;
    private String location;
    private String boxSize;

    public TolleryTransportCallSelectRequestBody(String cellId, String agvType, String storageCode, String location, String boxSize) {
        this.cellId = cellId;
        this.agvType = agvType;
        this.storageCode = storageCode;
        this.location = location;
        this.boxSize = boxSize;
    }
}

class TolleryTransportCallSelectAGFRequestBody {
    private String cellId;
    private String agvType;
    private String storageCode;
    private String location;
    private String height;

    public TolleryTransportCallSelectAGFRequestBody(String cellId, String agvType, String storageCode, String location, String height) {
        this.cellId = cellId;
        this.agvType = agvType;
        this.storageCode = storageCode;
        this.location = location;
        this.height = height;
    }
}

class TolleryTransportSendSelectAGFRequestBody {
    private String cellId;
    private String agvType;
    private String storageCode;
    private String location;
    private String height;

    public TolleryTransportSendSelectAGFRequestBody(String cellId, String agvType, String storageCode, String location, String height) {
        this.cellId = cellId;
        this.agvType = agvType;
        this.storageCode = storageCode;
        this.location = location;
        this.height = height;
    }
}

class TolleryTransportCallMoldRequestBody {
    private String cellId;
    private String agvType;

    public TolleryTransportCallMoldRequestBody(String cellId, String agvType) {
        this.cellId = cellId;
        this.agvType = agvType;
    }
}

class AutoTransportSendRequestBody {
    private String cartCode;
    private String cellId;
    private String productCode;
    private String agvType;

    public AutoTransportSendRequestBody(String cartCode, String cellId, String productCode, String agvType) {
        this.cartCode = cartCode;
        this.cellId = cellId;
        this.productCode = productCode;
        this.agvType = agvType;
    }
}

class TolleryTransportRequestBody {
    private String cellId;
    private String cartCode;


    public TolleryTransportRequestBody(String cellId, String cartCode) {
        this.cellId = cellId;
        this.cartCode = cartCode;
    }

    public TolleryTransportRequestBody(String cellId) {
        this.cellId = cellId;
    }
}

class FetchAvailableTrolleyRequestBody {
    private String cellId;

    public FetchAvailableTrolleyRequestBody(String cellId) {
        this.cellId = cellId;
    }

    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }
}


