package com.example.a00382071.parcelableknife.bean;

import com.example.ParcelKnife;

/**
 * Created by 00382071 on 2019/1/19.
 */
@ParcelKnife
public class LoginBean {
    /**
     * object : {"key":14252254,"code":"14252254","member":{"key":14252254,"code":"fefe527d-d6e6-42ad-bd35-b4e8de2552cf","mobileNumber":"18375918360"},"storeName":"小孩Milka","storeImage":"3","status":"ACTIVE","levelType":0,"teamMemberCount":0,"teamOrderCount":0}
     */

    public ObjectBean object;
    @ParcelKnife
    public static class ObjectBean {
        /**
         * key : 14252254
         * code : 14252254
         * member : {"key":14252254,"code":"fefe527d-d6e6-42ad-bd35-b4e8de2552cf","mobileNumber":"18375918360"}
         * storeName : 小孩Milka
         * storeImage : 3
         * status : ACTIVE
         * levelType : 0
         * teamMemberCount : 0
         * teamOrderCount : 0
         */

        public int key;
        public String code;
        public MemberBean member;
        public String storeName;
        public String storeImage;
        public String status;
        public int levelType;
        public int teamMemberCount;
        public int teamOrderCount;
        @ParcelKnife
        public static class MemberBean {
            /**
             * key : 14252254
             * code : fefe527d-d6e6-42ad-bd35-b4e8de2552cf
             * mobileNumber : 18375918360
             */

            public int key;
            public String code;
            public String mobileNumber;
        }
    }
}
