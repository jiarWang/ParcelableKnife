package com.example.a00382071.parcelableknife.bean;

import com.example.ParcelKnife;

/**
 * Created by 00382071 on 2019/1/19.
 */
@ParcelKnife
public class LoginBean {

    public ObjectBean object;
    @ParcelKnife
    public static class ObjectBean {

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

            public int key;
            public String code;
            public String mobileNumber;
        }
    }
}
