package com.example.a00382071.parcelableknife.bean;

import android.util.ArraySet;

import com.example.ParcelKnife;

import java.lang.reflect.Array;
import java.util.List;

/**
 * Created by 00382071 on 2019/1/19.
 */
@ParcelKnife
public class LoginBean {

    public ObjectBean object;
    public List<ObjectBean> objectBeanList;
    public List<String> objectBeanArray;
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
