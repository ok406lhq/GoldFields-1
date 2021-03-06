package com.szruito.goldfields.bean;

/**
 * Created by Administrator on 2017/7/10.
 */

public class AddGroupInfo {

    /**
     * code : 200
     * msg : 版本更新
     * data : {"versions_id":"1","appname":"GreenRoad.apk","appversion":"1.1","lastfalse":"2","downloadurl":"https://www.baidu.com","updateinfo":"1.sadjsajdas\r\n2.fdfdsfdsfdsf\r\n3.dfdfsfdsfdsfds"}
     */

    private int code;
    private String msg;
    private DataBean data;

    @Override
    public String toString() {
        return "UpdateAppInfo{" +
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

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * versions_id : 1
         * appname : GreenRoad.apk
         * appversion : 1.1
         * lastfalse : 2
         * downloadurl : https://www.baidu.com
         * updateinfo : 1.sadjsajdas
         2.fdfdsfdsfdsf
         3.dfdfsfdsfdsfds
         */

        private String group_type;
        private String versions_id;
        private String appname;
        private String appversion;
        private String lastfalse;
        private String downloadurl;
        private String updateinfo;
        private String version_code;

        public String getVersion_code() {
            return version_code;
        }

        public void setVersion_code(String version_code) {
            this.version_code = version_code;
        }

        public String getVersions_id() {
            return versions_id;
        }

        public void setVersions_id(String versions_id) {
            this.versions_id = versions_id;
        }

        public String getAppname() {
            return appname;
        }

        public void setAppname(String appname) {
            this.appname = appname;
        }

        public String getAppversion() {
            return appversion;
        }

        public void setAppversion(String appversion) {
            this.appversion = appversion;
        }

        public String getLastfalse() {
            return lastfalse;
        }

        public void setLastfalse(String lastfalse) {
            this.lastfalse = lastfalse;
        }

        public String getDownloadurl() {
            return downloadurl;
        }

        public void setDownloadurl(String downloadurl) {
            this.downloadurl = downloadurl;
        }

        public String getUpdateinfo() {
            return updateinfo;
        }

        public void setUpdateinfo(String updateinfo) {
            this.updateinfo = updateinfo;
        }

        public String getGroup_type() {
            return group_type;
        }

        public void setGroup_type(String group_type) {
            this.group_type = group_type;
        }
    }
}
