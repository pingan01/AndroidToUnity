package com.LingLing.SapientialTravel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BuyActivity extends Activity implements View.OnClickListener {
    public static final String TICKET_IMG = "http://keji.lingjing.com/zhl/api/app/GetBuyTicketImg";//获取线上支付门票的广告图
    public static final String USER_IMG = "http://keji.lingjing.com/zhl/api/app/GetUserImg";//获取用户头像
    public static final String GET_FEN = "http://keji.lingjing.com/zhl/api/app/getintegral";//获取积分

    public static final String TAG = "BuyActivity";
    private Button mBuyBack;
    private ListView mList;
    public TextView mTxtNickName;
    public TextView mTxtFen;
    public TextView mTxtnicheng;
    public TextView mTxtjifen;
    private ImageView mImgUser;
    private static String nickName;
    private static String pwd;
    private static String token;
    private ImageView mImgTicket;
    private int count;
    int[] imageTicket = {R.drawable.ticket_1, R.drawable.ticket_4, R.drawable.ticket__2, R.drawable.ticket_3, R.drawable.ticket_1, R.drawable.ticket_3};
    private List<Infomation> infomationLists = new ArrayList<>();//数据源

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);
        Bundle bundle = this.getIntent().getBundleExtra("bundle");
        infomationLists = bundle.getParcelableArrayList("infomations");
        nickName = bundle.getString("name");
        pwd = bundle.getString("pwd");
        token = bundle.getString("token");
        initView();
        requestByHttpGet();
        Log.e(TAG, "infomationLists长度：" + infomationLists.size() + "\n" + "昵称：" + nickName + "\n" + "pwd：" + pwd + "\n" + "口令:" + token);
        MyAdapter adapter = new MyAdapter();
        mList.setAdapter(adapter);
        mBuyBack.setOnClickListener(this);
        mImgTicket.setOnClickListener(this);
    }

    public static String getName() {
        return nickName;
    }

    public static String getPwd() {
        return pwd;
    }

    public static String getToken() {
        return token;
    }

    private void initView() {
        mBuyBack = (Button) findViewById(R.id.btn_buyBack);
        mList = (ListView) findViewById(R.id.lst);
        mImgTicket = (ImageView) findViewById(R.id.imageView);
        mTxtNickName = (TextView) findViewById(R.id.txt_nick);
        mTxtnicheng = (TextView) findViewById(R.id.txt_nicheng);
        mTxtjifen = (TextView) findViewById(R.id.txt_jifen);
        mTxtNickName.setText(nickName);
        mTxtFen = (TextView) findViewById(R.id.txt_fen);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "jianti.ttf");
        mTxtFen.setTypeface(typeface);
        mTxtNickName.setTypeface(typeface);
        mTxtnicheng.setTypeface(typeface);
        mTxtjifen.setTypeface(typeface);
        mImgUser = (ImageView) findViewById(R.id.img_user);

        Picasso.with(this).load(USER_IMG)
                .error(R.drawable.touxiang)
                .into(mImgUser);
        //get请求获取网络图片
        /**
         * new Thread(new Runnable() {
        @Override
        public void run() {
        HttpGet httpGet = new HttpGet(USER_IMG);
        httpGet.addHeader("token", token);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
        try {
        response = client.execute(httpGet);
        } catch (IOException e) {
        e.printStackTrace();
        }
        if (response.getStatusLine().getStatusCode() == 200) {
        HttpEntity entity = response.getEntity();
        try {
        InputStream content = entity.getContent();
        Bitmap bitmap = BitmapFactory.decodeStream(content);
        mImgUser.setImageBitmap(bitmap);
        } catch (IOException e) {
        e.printStackTrace();
        }
        }
        }
        }).start();
         */
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_buyBack || i == R.id.imageView) {
            finish();
        }
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return infomationLists.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (null == convertView) {
                convertView = LayoutInflater.from(BuyActivity.this).inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.mTxtContent = (TextView) convertView.findViewById(R.id.txt_number);
                holder.mImgContent = (ImageView) convertView.findViewById(R.id.img_spots);
                holder.mImgTicket = (ImageView) convertView.findViewById(R.id.img_ticket);
                holder.mBtnBuy = (Button) convertView.findViewById(R.id.btn_buy);
                holder.mTxtPrice = (TextView) convertView.findViewById(R.id.txt_ticket_price);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.mTxtContent.setText(infomationLists.get(position).getId() + "");
            holder.mImgTicket.setImageResource(imageTicket[position]);
            holder.mTxtPrice.setText(String.valueOf((infomationLists.get(position).getMoney()) / 100f));
            Picasso.with(BuyActivity.this)
                    .load(TICKET_IMG + "?id=" + infomationLists.get(position).getId())
                    .into(holder.mImgContent);

            holder.mBtnBuy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", position);
                    bundle.putString("token",token);
                    bundle.putParcelableArrayList("infomationLists", (ArrayList<? extends Parcelable>) infomationLists);
                    Intent intent = new Intent(BuyActivity.this, BuyDetailActivity.class);
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                }
            });
            return convertView;
        }

        class ViewHolder {
            TextView mTxtContent;
            ImageView mImgContent;
            ImageView mImgTicket;
            Button mBtnBuy;
            TextView mTxtPrice;
        }
    }

    private void requestByHttpGet() {
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
                            count = json.getInt("count");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTxtFen.setText(String.valueOf(count));
                                }
                            });
                            Log.e("TAG", "积分:" + count);
                        } else {
                            Log.e("TAG", "错误信息：" + json.getString("msg"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(BuyActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }
}
