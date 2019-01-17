package com.LingLing.SapientialTravel;

/**
 * Created by HPA on 2017/9/7.
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 票务信息类
 */
public class Infomation implements Parcelable, Parcelable.Creator {
    public int id;//门票的唯一主键
    public String name;//门票的名称
    public String useTimeType;//门票使用时间的模式--cumulative时表示门票限制使用时间为固定期间，
    // 为limitedtime时表示门票限制使用时间为购买后有固定的有效时间。
    public String useStartTime;//门票使用时间的起始--usetimetype为cumulative时有效
    public String useEndTime;//门票使用时间的结束--usetimetype为cumulative时有效
    public int useTimeHour;//门票购买后有效时间（小时）--usetimetype为limitedtime时有效
    public String conveStartTime;//门票兑换时间的起始
    public String conveEndTime;//门票兑换时间的结束
    public int money;//门票价格---服务器给的单位为：分
    public int surplusCount;//该门票剩余可兑换数量
    public String info;//门票说明
    public String remark;//门票备注
    public String type;//门票类型：alone为单景点门票，current为全场通票
    public String projName;//该门票使用的景点名称，通票时为null
    public boolean isImg;//true代表服务器后台设置了该门票的展示图。
    // False代表后台未上传，此时不应请求服务器获取图片，应该显示一张缺省图，或者不显示图。

    public Infomation() {

    }

    public Infomation(Parcel source) {

        id = source.readInt();
        name = source.readString();
        useTimeType = source.readString();
        useStartTime = source.readString();
        useEndTime = source.readString();
        useTimeHour = source.readInt();
        conveStartTime = source.readString();
        conveEndTime = source.readString();
        money = source.readInt();
        surplusCount = source.readInt();
        info = source.readString();
        remark = source.readString();
        type = source.readString();
        projName = source.readString();
        isImg = source.readInt() == 1;
    }

    public static final Creator<Infomation> CREATOR = new Creator<Infomation>() {
        @Override
        public Infomation createFromParcel(Parcel source) {
            return new Infomation(source);
        }

        @Override
        public Infomation[] newArray(int size) {
            return new Infomation[size];
        }
    };


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUseTimeType() {
        return useTimeType;
    }

    public void setUseTimeType(String useTimeType) {
        this.useTimeType = useTimeType;
    }

    public String getUseStartTime() {
        return useStartTime;
    }

    public void setUseStartTime(String useStartTime) {
        this.useStartTime = useStartTime;
    }

    public String getUseEndTime() {
        return useEndTime;
    }

    public void setUseEndTime(String useEndTime) {
        this.useEndTime = useEndTime;
    }

    public int getUseTimeHour() {
        return useTimeHour;
    }

    public void setUseTimeHour(int useTimeHour) {
        this.useTimeHour = useTimeHour;
    }

    public String getConveStartTime() {
        return conveStartTime;
    }

    public void setConveStartTime(String conveStartTime) {
        this.conveStartTime = conveStartTime;
    }

    public String getConveEndTime() {
        return conveEndTime;
    }

    public void setConveEndTime(String conveEndTime) {
        this.conveEndTime = conveEndTime;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getSurplusCount() {
        return surplusCount;
    }

    public void setSurplusCount(int surplusCount) {
        this.surplusCount = surplusCount;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProjName() {
        return projName;
    }

    public void setProjName(String projName) {
        this.projName = projName;
    }

    public boolean isImg() {
        return isImg;
    }

    public void setImg(boolean img) {
        isImg = img;
    }

    @Override
    public String toString() {
        return "Infomation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", useTimeType='" + useTimeType + '\'' +
                ", useStartTime='" + useStartTime + '\'' +
                ", useEndTime='" + useEndTime + '\'' +
                ", useTimeHour=" + useTimeHour +
                ", conveStartTime='" + conveStartTime + '\'' +
                ", conveEndTime='" + conveEndTime + '\'' +
                ", money=" + money +
                ", surplusCount=" + surplusCount +
                ", info='" + info + '\'' +
                ", remark='" + remark + '\'' +
                ", type='" + type + '\'' +
                ", projName='" + projName + '\'' +
                ", isImg=" + isImg +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(useTimeType);
        dest.writeString(useStartTime);
        dest.writeString(useEndTime);
        dest.writeInt(useTimeHour);
        dest.writeString(conveStartTime);
        dest.writeString(conveEndTime);
        dest.writeInt(money);
        dest.writeInt(surplusCount);
        dest.writeString(info);
        dest.writeString(remark);
        dest.writeString(type);
        dest.writeString(projName);
        dest.writeInt(isImg==true?1:0);
    }

    @Override
    public Object createFromParcel(Parcel source) {
        return null;
    }

    @Override
    public Object[] newArray(int size) {
        return new Object[0];
    }
}
