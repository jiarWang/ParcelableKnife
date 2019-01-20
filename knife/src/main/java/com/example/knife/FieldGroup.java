package com.example.knife;

import com.example.ParcelKnife;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;

/**
 * Created by 00382071 on 2019/1/18.
 */

public class FieldGroup {
    private static final String SUFFIX = "Model";
    private static final ClassName PARCEL_ABLE_CLASS_NAME = ClassName.get("android.os", "Parcelable");
    private static final ClassName PARCEL_CLASS_NAME = ClassName.get("android.os", "Parcel");
    private static final ClassName CREATOR_CLASS_NAME = ClassName.get("android.os", "Parcelable", "Creator");
    private String qualifiedClassName;
    private Map<String, TypeMirror> itemMap = new LinkedHashMap<>();
    private List<VariableElement> fields;
    private String beanTag = "tag";

    public FieldGroup(TypeElement typeElement) {
        this.qualifiedClassName = typeElement.getQualifiedName().toString();
        beanTag = typeElement.getAnnotation(ParcelKnife.class).beanTag();
        fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
        for (VariableElement fieldElement : fields) {
            addField(fieldElement.getSimpleName().toString(), fieldElement.asType());
        }
    }


    private void addField(String fieldName, TypeMirror fieldClass) {
        itemMap.put(fieldName, fieldClass);
    }

    public void generateCode(Elements elementUtils, Filer filer) throws IOException {
        TypeElement currentClassElement = elementUtils.getTypeElement(qualifiedClassName);

        String knifeClassSimpleName = currentClassElement.getSimpleName().toString() + SUFFIX;
        PackageElement packageElement = elementUtils.getPackageOf(currentClassElement);
        String knifePackageName = packageElement.isUnnamed() ? null : packageElement.getQualifiedName().toString();


        StringBuilder readBodyStr = new StringBuilder();
        StringBuilder writeBodyStr = new StringBuilder();
        //fields
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        for (Map.Entry<String, TypeMirror> entry : itemMap.entrySet()) {
            TypeName fieldClassName = getTypeName(entry.getValue().getKind());
            //非基础类型
            if (fieldClassName == TypeName.OBJECT) {
                fieldClassName = getModelTypeNameForField(entry.getValue().toString());
                //fieldClassName = ClassName.get(entry.getValue());
            }
            FieldSpec fieldSpec = FieldSpec.builder(fieldClassName, entry.getKey())
                    .addModifiers(Modifier.PUBLIC)
                    .addJavadoc("")
                    .build();
            fieldSpecs.add(fieldSpec);
            readBodyStr.append(getInForm(entry.getValue().getKind(), entry.getKey(), getSimpleNameByCanonicalNameForModel(entry.getValue().toString())));
            writeBodyStr.append(getOutForm(entry.getValue().getKind(), entry.getKey(), getSimpleNameByCanonicalNameForModel(entry.getValue().toString())));
        }
        //Creator
        FieldSpec creator = FieldSpec.builder(
                ParameterizedTypeName.get(CREATOR_CLASS_NAME, ClassName.get(knifePackageName, knifeClassSimpleName)),
                "CREATOR",
                Modifier.PUBLIC,
                Modifier.FINAL,
                Modifier.STATIC)
                .initializer(String.format(CREATOR_CODE_FORM,
                        knifeClassSimpleName,
                        knifeClassSimpleName,
                        knifeClassSimpleName,
                        knifeClassSimpleName,
                        knifeClassSimpleName))
                .build();

        //constructor
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addParameter(PARCEL_CLASS_NAME, "in")
                .addCode(readBodyStr.toString())
                .build();

        //Creator

        //describeContents
        MethodSpec describeContents = MethodSpec.methodBuilder("describeContents")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(int.class)
                .addCode("return 0;\n")
                .build();


        //write to
        MethodSpec write = MethodSpec.methodBuilder("writeToParcel")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addCode(writeBodyStr.toString())
                .addParameter(PARCEL_CLASS_NAME, "parcel")
                .addParameter(int.class, "i")
                .build();
        //java file content
        TypeSpec codeJava = TypeSpec.classBuilder(knifeClassSimpleName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(PARCEL_ABLE_CLASS_NAME)
                .addJavadoc(String.format("BeanTag <==> %s", beanTag.replaceAll("\\$", ".")))
                .addFields(fieldSpecs)
                .addMethod(constructor)
                .addField(creator)
                .addMethod(describeContents)
                .addMethod(write)
                .build();


        JavaFile.builder(knifePackageName, codeJava).build().writeTo(filer);
    }

    private static TypeName getTypeName(TypeKind typeKind) {
        if (typeKind == TypeKind.BOOLEAN) {
            return TypeName.BOOLEAN;
        } else if (typeKind == TypeKind.BYTE) {
            return TypeName.BYTE;
        } else if (typeKind == TypeKind.SHORT) {
            return TypeName.BOOLEAN;
        } else if (typeKind == TypeKind.INT) {
            return TypeName.INT;
        } else if (typeKind == TypeKind.LONG) {
            return TypeName.LONG;
        } else if (typeKind == TypeKind.CHAR) {
            return TypeName.CHAR;
        } else if (typeKind == TypeKind.FLOAT) {
            return TypeName.FLOAT;
        } else if (typeKind == TypeKind.DOUBLE) {
            return TypeName.DOUBLE;
        }
        return TypeName.OBJECT;
    }


    private static TypeName getModelTypeNameForField(String classCanonicalName) {
        TypeName resultTypeName = PARCEL_ABLE_CLASS_NAME;
        String packageName = "", classSimpleName = "";
        if (classCanonicalName.startsWith("java.lang")){
            packageName = "java.lang";
            classSimpleName = classCanonicalName.substring(10);// 10 = "java.lang.".length()
            resultTypeName = ClassName.get(packageName, classSimpleName);
        }else if(classCanonicalName.startsWith("java.util")){
            packageName = "java.util";
            classSimpleName = classCanonicalName.substring(10);// 10 = "java.util.".length()
            String insideClassName = classSimpleName.substring(
                    classSimpleName.indexOf("<") + 1,
                    classSimpleName.indexOf(">")
            ).replaceAll("\\.([A-Z])", "\\$$1");

            String insidePackageName = insideClassName.substring(
                    0,
                    insideClassName.indexOf("$")
            );
            StringBuffer insideTypename = new StringBuffer();
            insideTypename.append(classSimpleName.substring(
                    classSimpleName.lastIndexOf(".") + 1,
                    classSimpleName.length() - 1
            ));
            System.out.println("---- insidePackageName ->" + insidePackageName);
            if (!(insidePackageName.startsWith("java.lang") || insidePackageName.startsWith("java.util"))){
                insideTypename.append(SUFFIX);
            }
            resultTypeName = ParameterizedTypeName.get(
                    ClassName.get(packageName, classSimpleName.substring(0, classSimpleName.indexOf("<"))),
                    ClassName.get(
                            insidePackageName,
                            insideTypename.toString()
                    )
            );
        }else {
            if (classCanonicalName.contains("<")){
                try {
                    throw new Exception("the Type of field should not be ParameterizedType");
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            String className = classCanonicalName.replaceAll("\\.([A-Z]+)","\\$$1");
            System.out.println(String.format("\n %s,\n %s", classCanonicalName, className));
            String[] classInfo = className.split("\\$", 2);
            packageName = classInfo[0];
            classSimpleName = className.substring(className.lastIndexOf("$") + 1, className.length()) + SUFFIX ;
            resultTypeName = ClassName.get(packageName, classSimpleName);
        }
        return resultTypeName;
    }
    private static String getSimpleNameByCanonicalNameForModel(String canonicalName){
        if (canonicalName.contains("java.util") && canonicalName.contains("<")){
            String insideClassName = canonicalName.substring(canonicalName.lastIndexOf("<") + 1, canonicalName.length() - 1);
            String modelName = insideClassName.substring(insideClassName.lastIndexOf(".") + 1, insideClassName.length());
            if(insideClassName.startsWith("java.util") || insideClassName.startsWith("java.lang")){
            }else{
                modelName += SUFFIX;
            }

            return canonicalName.substring(10, canonicalName.indexOf("<"))
                    + "|"
                    + modelName;
        }
        String modelName = canonicalName.replaceAll("\\.([A-Z]+)", "\\$$1");
        String[] ss = modelName.split("\\$");
        String resultClassName = null;
        String packageName = "";


        if (ss.length >= 2) {
            packageName= ss[0];
            resultClassName = ss[ss.length - 1];
            //todo 设置白名单
            if (!packageName.equals("java.lang") && !packageName.equals("java.util")) {
                resultClassName = resultClassName + SUFFIX;
            }
        }
        return resultClassName ;
    }
    //获取Constructor(Parcel parcel)内部书写格式
    private static final String getInForm(TypeKind typeKind,String fieldName, String filedModelSimpleName){
        if (typeKind == TypeKind.BOOLEAN) {
            return String.format("%s = in.readByte(%s != 0 );\n", fieldName, fieldName);
        } else if (typeKind == TypeKind.BYTE) {
            return String.format("%s = in.readByte();\n", fieldName);
        } else if (typeKind == TypeKind.SHORT) {
            return String.format("%s = (short)in.readInt();\n", fieldName);
        } else if (typeKind == TypeKind.INT) {
            return String.format("%s = in.readInt();\n", fieldName);
        } else if (typeKind == TypeKind.LONG) {
            return String.format("%s = in.readLong();\n", fieldName);
        }  else if (typeKind == TypeKind.FLOAT) {
            return String.format("%s = in.readFloat();\n", fieldName);
        } else if (typeKind == TypeKind.DOUBLE) {
            return String.format("%s = in.readDouble();\n", fieldName);
        }
        if (filedModelSimpleName.equals( "String")){
            return String.format("%s = in.readString();\n", fieldName);
        }
        if (filedModelSimpleName.contains("|")){
            String insideTypeName = filedModelSimpleName.split("\\|")[1];
            if (insideTypeName.contains(SUFFIX)){
                return String.format("%s = in.createTypedArrayList(%s.CREATOR);\n", fieldName, filedModelSimpleName.split("\\|")[1]);
            }else{
                return String.format("%s = in.readArrayList(%s.class.getClassLoader());\n", fieldName, filedModelSimpleName.split("\\|")[1]);
            }

        }
        return String.format("%s = in.readParcelable(%s.class.getClassLoader());\n", fieldName, filedModelSimpleName);
    }

    private static final String getOutForm(TypeKind typeKind,String fieldName, String filedModelSimpleName){
        if (typeKind == TypeKind.BOOLEAN) {
            return String.format("parcel.writeByte((byte)(%s ? 0 : 1 ) );\n", fieldName, fieldName);
        } else if (typeKind == TypeKind.BYTE) {
            return String.format("parcel.writeByte(%s);\n", fieldName);
        } else if (typeKind == TypeKind.SHORT) {
            return String.format("parcel.writeInt(%s);\n", fieldName);
        } else if (typeKind == TypeKind.INT) {
            return String.format("parcel.writeInt(%s);\n", fieldName);
        } else if (typeKind == TypeKind.LONG) {
            return String.format("parcel.writeLong(%s);\n", fieldName);
        }  else if (typeKind == TypeKind.FLOAT) {
            return String.format("parcel.writeFloat(%s);\n", fieldName);
        } else if (typeKind == TypeKind.DOUBLE) {
            return String.format("parcel.writeDouble(%s);\n", fieldName);
        }
        if (filedModelSimpleName.equals( "String")){
            return String.format("parcel.writeString(%s);\n", fieldName);
        }
        if (filedModelSimpleName.contains("|")){
            String insideTypeName = filedModelSimpleName.split("\\|")[1];
            if (insideTypeName.contains(SUFFIX)){
                return String.format("parcel.writeTypedList(%s);\n", fieldName, filedModelSimpleName.split("\\|")[1]);
            }else{
                return String.format("parcel.writeStringList(%s);\n", fieldName, filedModelSimpleName.split("\\|")[1]);
            }
        }
        return String.format("parcel.writeParcelable(%s, i);\n", fieldName, filedModelSimpleName);
    }

    //5 %s
    private static final String CREATOR_CODE_FORM =
            "new Creator<%s>() {\n" +
                    "        @Override\n" +
                    "        public %s createFromParcel(Parcel in) {\n" +
                    "            return new %s(in);\n" +
                    "        }\n" +
                    "\n" +
                    "        @Override\n" +
                    "        public %s[] newArray(int size) {\n" +
                    "            return new %s[size];\n" +
                    "        }\n" +
                    "    }";
}
