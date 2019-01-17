package com.LingLing.SapientialTravel.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.LingLing.SapientialTravel.BuyActivity;
import com.LingLing.SapientialTravel.BuyDetailActivity;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI api;
    private static final String CHECK = "http://keji.lingjing.com/zhl/api/app/CheckOrder";//检查订单支付状态
    private static final String TAG = "WXPayEntryActivity";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = BuyDetailActivity.getWXAPI(this);
        api.handleIntent(getIntent(), this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        finish();
    }

    @Override
    public void onResp(BaseResp resp) {
        //errCode: 0, success;
        // -1：可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
        // error; -2, cancel by user无需处理。发生场景：用户不支付了，点击取消，返回APP
        switch (resp.errCode) {
            case 0: //用户同意
                if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
                    Toast.makeText(this, "支付成功", Toast.LENGTH_SHORT).show();
                    finish();
                    //去查询订单
                    //与服务器进行通信==post方式
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HttpPost post = new HttpPost(CHECK + "?out_trade_no=" + BuyDetailActivity.getTrade());
                            post.addHeader("token", BuyActivity.getToken());
                            /**
                             * List<NameValuePair> params = new ArrayList<>();
                             params.add(new BasicNameValuePair("phone", BuyActivity.getName()));
                             params.add(new BasicNameValuePair("pwd", BuyActivity.getPwd()));
                             params.add(new BasicNameValuePair("out_trade_no", BuyDetailActivity.getTrade()));
                             Log.e(TAG, "订单号：" + BuyDetailActivity.getTrade());
                             //设置字符集
                             HttpEntity entity = null;
                             try {
                             entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                             } catch (UnsupportedEncodingException e) {
                             e.printStackTrace();
                             }
                             post.setEntity(entity);
                             */
                            HttpClient client = new DefaultHttpClient();
                            HttpResponse response = null;
                            try {
                                response = client.execute(post);
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
                                    String DINGDAN = json.getString("out_trade_no");
                                    String STATE = json.getString("state");
                                    Log.e("TAG", "success:" + success + "\n" + "订单号:" + DINGDAN + "\n" + "订单状态：" + STATE);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(WXPayEntryActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    }).start();
                }
                break;
            default:
                if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
                    Toast.makeText(this, "支付失败", Toast.LENGTH_SHORT).show();
                    WXPayEntryActivity.this.finish();
                    break;
                }
        }
    }
}