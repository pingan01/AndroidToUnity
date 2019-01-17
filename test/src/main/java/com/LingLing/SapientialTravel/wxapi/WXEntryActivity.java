package com.LingLing.SapientialTravel.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.LingLing.SapientialTravel.BuyActivity;
import com.LingLing.SapientialTravel.BuyDetailActivity;
import com.LingLing.SapientialTravel.FindActivity;
import com.LingLing.SapientialTravel.MainActivity;
import com.LingLing.SapientialTravel.UnityActivity;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI api;
    private static final String TAG = "WXEntryActivity";
    private String LOGIN = "http://keji.lingjing.com/zhl/api/app/wxlogin";
    // 获取第一步的code后，请求以下链接获取access_token
    private String GetCodeRequest = "https://api.weixin.qq.com/sns/oauth2/access_token?";
    // 获取用户个人信息
    private String GetUserInfo = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";
    private String WX_APP_SECRET = "3a52f1fb5ffc05c7c0203d8ae5c52189";//APP密钥

    private String code;

    public static final String GET_FEN = "http://keji.lingjing.com/zhl/api/app/getintegral";//获取积分
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //api = BuyDetailActivity.getWXAPI(this);
        api = WXAPIFactory.createWXAPI(this, "wx5ed5b0f7415d95b7", true);
        api.registerApp("wx5ed5b0f7415d95b7");//注册api
        if (!api.isWXAppInstalled()) {
            Toast.makeText(this, "未安装微信", Toast.LENGTH_SHORT).show();
        }
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
        finish();
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                Toast.makeText(this, "登陆成功", Toast.LENGTH_SHORT).show();
                SendAuth.Resp auth = (SendAuth.Resp) baseResp;
                code = auth.code;
                finish();
                //loginPanel(Clone)
                Log.e(TAG, "code==" + code);
                //Log.e(TAG, "unity传输过来的数据:" + UnityActivity.number);
                UnityPlayer.UnitySendMessage("loginPanel(Clone)", "message",code);//点击按钮后android程序给unity发消息
                finish();
                //确保
                Get();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                Toast.makeText(this, "发送取消", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                Toast.makeText(this, "发送被拒绝", Toast.LENGTH_SHORT).show();
                finish();
                break;
            default:
                Toast.makeText(this, "发送返回", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
        finish();
    }

    private void Get() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "1  code=="+code);
                HttpGet get = new HttpGet(LOGIN + "?code=" + code);
                Log.e(TAG, "get==" + get.toString());
                HttpClient client = new DefaultHttpClient();
                Log.e(TAG, "3");
                HttpResponse response = null;
                try {
                    Log.e(TAG, "4");
                    response = client.execute(get);
                    Log.e(TAG, "响应体:" + response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (response != null) {
                    //判断请求是否成功
                    if (response.getStatusLine().getStatusCode() == 200) {
                        //返回数据
                        String result = null;
                        try {

                            Log.e(TAG, "5");
                            result = EntityUtils.toString(response.getEntity(), "UTF-8");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.e("TAG", "返回结果==" + result);
                        try {
                            JSONObject json = new JSONObject(result);
                            String token = json.getString("token");
                            UnityPlayer.UnitySendMessage("loginPanel(Clone)","getToken",token);
                            Log.e(TAG, "token==" + token);
                            requestByHttpGet(token);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(WXEntryActivity.this, "返回错误", Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }
    private void requestByHttpGet(final String token) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpGet httpGet = new HttpGet(GET_FEN);
                httpGet.addHeader("token", token);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = null;
                try {
                    response = client.execute(httpGet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //判断请求是否成功
                if (response.getStatusLine().getStatusCode() == 200) {
                    //返回数据
                    String result = null;
                    try {
                        result = EntityUtils.toString(response.getEntity(), "UTF-8");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("TAG", "返回结果==" + result);
                    try {
                        JSONObject json = new JSONObject(result);
                        String success = json.getString("result");
                        Log.e("TAG", "是否成功:" + success);
                        if (success.equals("success")) {
                            Log.e("TAG", "积分:" + json.getInt("count"));
                        } else {
                            Log.e("TAG", "错误信息：" + json.getString("msg"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(WXEntryActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }
}
