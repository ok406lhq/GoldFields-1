package com.szruito.goldfields.bean;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/7/10.
 */

public class RemoveGroupInfo {

    /**
     * code : 200
     * msg : 版本更新
     * data : {"versions_id":"1","appname":"GreenRoad.apk","appversion":"1.1","lastfalse":"2","downloadurl":"https://www.baidu.com","updateinfo":"1.sadjsajdas\r\n2.fdfdsfdsfdsf\r\n3.dfdfsfdsfdsfds"}
     */

    private int code;
    private String msg;
    private ArrayList<String> data;

    @Override
    public String toString() {
        return "RemoveGroupInfo{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ArrayList<String> getData() {
        return data;
    }

    public void setData(ArrayList<String> data) {
        this.data = data;
    }
}
