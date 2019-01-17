package com.LingLing.SapientialTravel;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BuyDetailActivity extends Activity implements View.OnClickListener {

    private Button mBtnBack;
    private int position;
    private Button mBtnPay;
    private ImageView mImgPay;
    private TextView mTxtPay;
    int[] imageTicket = {R.drawable.ticket_1, R.drawable.ticket_4, R.drawable.ticket__2, R.drawable.ticket_3, R.drawable.ticket_1, R.drawable.ticket_3};
    List<Infomation> detailInfos = new ArrayList<>();

    //微信支付
    private static String APP_ID;
    private String MCH_ID;
    public String PRE_PAY;
    public static String DINGDAN;
    private String PACKAGE;
    private String NONCE;
    private String timeStamp;
    private String SIGN;
    public static final String PAY = "http://keji.lingjing.com/zhl/api/app/CreateOrder";//微信统一下单

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_detail);
        Bundle bundle = this.getIntent().getBundleExtra("bundle");
        position = bundle.getInt("position", -1);
        detailInfos = bundle.getParcelableArrayList("infomationLists");
        requestByHttpGet();
        initView();
        mImgPay.setImageResource(imageTicket[position]);
        mTxtPay.setText(String.valueOf((detailInfos.get(position).getMoney()) / 100f));
        mBtnBack.setOnClickListener(this);
        mBtnPay.setOnClickListener(this);
    }

    private void initView() {
        mBtnBack = (Button) findViewById(R.id.btn_detail_back);
        mImgPay = (ImageView) findViewById(R.id.img_pay);
        mBtnPay = (Button) findViewById(R.id.btn_pay);
        mTxtPay = (TextView) findViewById(R.id.pay_money);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_detail_back) {
            finish();

        } else if (i == R.id.btn_pay) {//点击"立即下单后进行微信支付操作"
            IWXAPI wxapi = getWXAPI(BuyDetailActivity.this);
            PayReq req = new PayReq();
            Log.e("TAG", "req==" + req);
            req.appId = APP_ID;//APPID
            Log.e("TAG", "APPID==" + APP_ID);
            req.partnerId = MCH_ID;//商户ID

            req.prepayId = PRE_PAY;//预支付交易会话ID
            req.packageValue = PACKAGE;//扩展字段==暂填写固定值Sign=WXPay

            req.nonceStr = NONCE;//随机字符串
            req.timeStamp = timeStamp;//时间戳
            Log.e("TAG", "timeStamp==" + req.timeStamp);
            req.sign = SIGN;//签名
            Log.e("TAG", "sign==" + req.sign);
            wxapi.sendReq(req);
            Toast.makeText(BuyDetailActivity.this, "向微信客户端发送请求", Toast.LENGTH_SHORT).show();
        }
    }

    public static IWXAPI getWXAPI(Context context) {
        IWXAPI api = WXAPIFactory.createWXAPI(context, "wx5ed5b0f7415d95b7");
        api.registerApp("wx5ed5b0f7415d95b7");//注册api
        if (!api.isWXAppInstalled()) {
            Toast.makeText(context, "未安装微信", Toast.LENGTH_SHORT).show();
        }
        return api;
    }

    public static String getAppId() {
        return APP_ID;
    }

    public static String getTrade() {
        return DINGDAN;
    }

    //与服务器进行通信==post方式
    private void requestByHttpGet() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("TAG","1");
                HttpGet get = new HttpGet(PAY + "?ticketid=" + detailInfos.get(position).getId());
                Log.e("TAG","2");
                get.addHeader("token", BuyActivity.getToken());
                /**
                 * List<NameValuePair> params = new ArrayList<>();
                 params.add(new BasicNameValuePair("phone", BuyActivity.getName()));
                 params.add(new BasicNameValuePair("pwd", BuyActivity.getPwd()));
                 params.add(new BasicNameValuePair("ticketid", detailInfos.get(position).getId() + ""));
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
                    response = client.execute(get);
                    Log.e("TAG","3");
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
                        APP_ID = json.getString("appid");
                        MCH_ID = json.getString("partnerid");
                        PRE_PAY = json.getString("prepayid");
                        PACKAGE = json.getString("package");
                        DINGDAN = json.getString("out_trade_no");
                        NONCE = json.getString("noncestr");
                        timeStamp = json.getString("timestamp");
                        SIGN = json.getString("sign");
                        Log.e("TAG", "success:" + success + "\n" + "APP_ID:" + APP_ID + "\n" + "商户ID:" + MCH_ID + "\n" + "预支付ID:" + PRE_PAY + "\n" + "订单号:" + DINGDAN
                                + "\n" + "随机字符串：" + NONCE + "\n" + "时间戳：" + timeStamp + "\n" + "签名：" + SIGN + "\n" + "扩展字段：" + PACKAGE
                        );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(BuyDetailActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }
}
