package com.example.a00382071.parcelableknife.bean;

import android.util.ArraySet;

import com.example.ParcelKnife;

import java.lang.reflect.Array;
import java.util.List;

/**
 * Created by 00382071 on 2019/1/19.
 */
@ParcelKnife(beanTag = "LoginBean")
public class LoginBean {

    public int levelType;
    public int teamMemberCount;
    public int teamOrderCount;
    public ObjectBean object;
    public List<ObjectBean> objectBeanList;
    public List<String> objectBeanArray;
    @ParcelKnife(beanTag = "LoginBean$ObjectBean")
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
        @ParcelKnife(beanTag = "LoginBean$ObjectBean$MemberBean")
        public static class MemberBean {

            public int key;
            public String code;
            public String mobileNumber;
        }
    }
}
