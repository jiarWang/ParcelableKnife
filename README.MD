# ParcelKnife

[`ParcelKnife`](https://github.com/jiarWang/ParcelableKnife/blob/master/knife.jar):一款通过注解可以由`xxxBean`自动生成`xxxBeanModel implements Parcelable`的工具。

## 描述

在`Android`开发中，通常需要为后台接口返回的`JSON`数据建立一个`xxxBean`类来进行`JSON`数据的解析。

解析工作完成后，客户端便可以使用`xxxBean`的实例对象进行操作，然而如果想让

该对象在`Activity`之间或者进程之间通讯的话，`xxxBean`对象就需要是可序列化对象。

`Android SDK` 为普通类提供了两种序列化的方式，一种是实现`Parcelable`接口，另一种则是实现`Serializable`接口。

在可序列化对象用于网络通讯或磁盘文件的读写时，通常采用实现`Serializable`的方式比较便捷。其他情况下，官方建议通过实现`Parcelable`接口的方式实现对象的序列化。

可是！！！实现`Parcelable`接口的类书写通常重复且复杂。

通过`ParcelKnife`，你将只需要编写<b>一条注解</b>便能生成一个与`xxxBean`相关联的`Parcelable`类（`xxxBeanModel`）

## 入门

1) 下载与导入`knife.jar`

下载[`knife.jar`](https://github.com/jiarWang/ParcelableKnife/blob/master/knife.jar)，并将`jar`包放入`MyApplication/app/libs`；

2) 添加注解处理器与`Javapoet`相关依赖，并应用`ParcelKnife`
```gradle
dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    compile 'com.squareup:javapoet:1.9.0'
    compile files('libs/knife.jar')
}
```
> 由于还没有上传 `maven`与`jcenter`库，故暂时只能以此法导入项目
3) 使用`@ParcelKnife(beanTag="YourClassName")`

通过在需要序列化的类上添加`@ParcelKnife(beanTag="YourClassName")`，然后点击"Make Project"便能自动生成`xxxBeanModel`;
例如：

如下是一个需要序列话的类，该类被用于解析登录接口的返回数据。现在需要一个实现了`Parcelable`的该类用于进程见通讯，如果直接将3个类直接`implements Parcelable`则代码阅读困难，且代码可读性极差。
通过使用使用`ParcelKnife`,编译器将自动为你生成3个实现了`Parcelable`接口的`BeanModel`类。`ParcelKnife`并不会改变量的名称，在其他地方你可以像使用`Bean`一样使用`BeanModel`。
```java
//: LoginBean.java
//第一处使用
@ParcelKnife(beanTag = "LoginBean")
public class LoginBean {

    public int levelType;
    public ObjectBean object;
    public List<ObjectBean> objectBeanList;
    public List<String> objectBeanArray;
    //第二处使用
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
        //第三处使用
        @ParcelKnife(beanTag = "LoginBean$ObjectBean$MemberBean")
        public static class MemberBean {

            public int key;
            public String code;
            public String mobileNumber;
        }
    }
}
```
执行"Make Project"后，将自动生成3个`BeanModel`(目录：`ProjectName/app/build/generated/source/apt/debug/xxxxx`),；
|
|--LoginBeanModel.java
|--MemberBeanModel.java
|--ObjectBeanModel.java

### LoginBeanModel.java
```java
/**
 * BeanTag <==> LoginBean */
public class LoginBeanModel implements Parcelable {
  public static final Parcelable.Creator<LoginBeanModel> CREATOR = new Creator<LoginBeanModel>() {
          @Override
          public LoginBeanModel createFromParcel(Parcel in) {
              return new LoginBeanModel(in);
          }

          @Override
          public LoginBeanModel[] newArray(int size) {
              return new LoginBeanModel[size];
          }
      };

  public int levelType;

  public int teamMemberCount;

  public int teamOrderCount;

  public ObjectBeanModel object;

  public List<ObjectBeanModel> objectBeanList;

  public List<String> objectBeanArray;

  LoginBeanModel(Parcel in) {
    levelType = in.readInt();
    teamMemberCount = in.readInt();
    teamOrderCount = in.readInt();
    object = in.readParcelable(ObjectBeanModel.class.getClassLoader());
    objectBeanList = in.createTypedArrayList(ObjectBeanModel.CREATOR);
    objectBeanArray = in.readArrayList(String.class.getClassLoader());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeInt(levelType);
    parcel.writeInt(teamMemberCount);
    parcel.writeInt(teamOrderCount);
    parcel.writeParcelable(object, i);
    parcel.writeTypedList(objectBeanList);
    parcel.writeStringList(objectBeanArray);
  }
```
### MemberBeanModel.java
```java

/**
 * BeanTag <==> LoginBean.ObjectBean.MemberBean */
public class MemberBeanModel implements Parcelable {
  public static final Parcelable.Creator<MemberBeanModel> CREATOR = new Creator<MemberBeanModel>() {
          @Override
          public MemberBeanModel createFromParcel(Parcel in) {
              return new MemberBeanModel(in);
          }

          @Override
          public MemberBeanModel[] newArray(int size) {
              return new MemberBeanModel[size];
          }
      };

  public int key;

  public String code;

  public String mobileNumber;

  MemberBeanModel(Parcel in) {
    key = in.readInt();
    code = in.readString();
    mobileNumber = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeInt(key);
    parcel.writeString(code);
    parcel.writeString(mobileNumber);
  }
}
```
### ObjectBeanModel.java
```java
/**
 * BeanTag <==> LoginBean.ObjectBean */
public class ObjectBeanModel implements Parcelable {
  public static final Parcelable.Creator<ObjectBeanModel> CREATOR = new Creator<ObjectBeanModel>() {
          @Override
          public ObjectBeanModel createFromParcel(Parcel in) {
              return new ObjectBeanModel(in);
          }

          @Override
          public ObjectBeanModel[] newArray(int size) {
              return new ObjectBeanModel[size];
          }
      };

  public int key;

  public String code;

  public MemberBeanModel member;

  public String storeName;

  public String storeImage;

  public String status;

  public int levelType;

  public int teamMemberCount;

  public int teamOrderCount;

  ObjectBeanModel(Parcel in) {
    key = in.readInt();
    code = in.readString();
    member = in.readParcelable(MemberBeanModel.class.getClassLoader());
    storeName = in.readString();
    storeImage = in.readString();
    status = in.readString();
    levelType = in.readInt();
    teamMemberCount = in.readInt();
    teamOrderCount = in.readInt();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeInt(key);
    parcel.writeString(code);
    parcel.writeParcelable(member, i);
    parcel.writeString(storeName);
    parcel.writeString(storeImage);
    parcel.writeString(status);
    parcel.writeInt(levelType);
    parcel.writeInt(teamMemberCount);
    parcel.writeInt(teamOrderCount);
  }
}
```
4) 说明
* 由于所有的`xxxBeanModel`均在同一`package`下，所以确保在所有需要使用`ParcelKnife`的`xxxBean`类中类的类名不能有重复。