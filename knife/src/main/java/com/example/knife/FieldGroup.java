package com.example.knife;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private static final String SUFFIX = "BindingModel";
    private static final ClassName PARCEL_ABLE_CLASS_NAME = ClassName.get("android.os", "Parcelable");
    private static final ClassName PARCEL_CLASS_NAME = ClassName.get("android.os", "Parcel");
    private static final ClassName CREATOR_CLASS_NAME = ClassName.get("android.os", "Parcelable", "Creator");
    private String qualifiedClassName;
    private Map<String, TypeMirror> itemMap = new LinkedHashMap<>();
    private List<VariableElement> fields;

    public FieldGroup(TypeElement typeElement) {
        this.qualifiedClassName = typeElement.getQualifiedName().toString();
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

        //fields
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        for (Map.Entry<String, TypeMirror> entry : itemMap.entrySet()) {
            TypeName fieldClassName = getTypeName(entry.getValue().getKind());
            if (fieldClassName == TypeName.OBJECT) {
                fieldClassName = getClassNameByName(entry.getValue().toString());
            }
            FieldSpec fieldSpec = FieldSpec.builder(fieldClassName, entry.getKey())
                    .addModifiers(Modifier.PUBLIC)
                    .addJavadoc("")
                    .build();
            fieldSpecs.add(fieldSpec);
            readBodyStr.append(getInForm(entry.getValue().getKind(), entry.getKey(), getModelSimpleNameByCanonicalName(entry.getValue().toString())));
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
                .addParameter(PARCEL_CLASS_NAME, "parcel")
                .addParameter(int.class, "i")
                .build();
        //java file content
        TypeSpec codeJava = TypeSpec.classBuilder(knifeClassSimpleName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(PARCEL_ABLE_CLASS_NAME)
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

    private String getCanonicalNameBySimpleName(String className) {
        return className.replaceAll("([A-Z]+[\\w]*)\\.", "$1\\$");
    }

    private static ClassName getClassNameByName(String className) {
        String canonicalName = className.replaceAll("\\.([A-Z]+)", "\\$$1");
        String[] ss = canonicalName.split("\\$");
        ClassName resultClassName = null;
        if (ss.length >= 2) {
            String packageName = ss[0];
            String typeName = ss[ss.length - 1];
            //todo 设置白名单
            System.out.println("--------" + packageName);
            if (!packageName.equals("java.lang")) {
                typeName = typeName + SUFFIX;
            }
            resultClassName = ClassName.get(packageName, typeName);
        } else {
            resultClassName = PARCEL_ABLE_CLASS_NAME;
        }
        return resultClassName;
    }
    private static String getModelSimpleNameByCanonicalName(String canonicalName){
        String modelName = canonicalName.replaceAll("\\.([A-Z]+)", "\\$$1");
        String[] ss = modelName.split("\\$");
        String resultClassName = null;
        if (ss.length >= 2) {
            String packageName = ss[0];
            resultClassName = ss[ss.length - 1];
            //todo 设置白名单

            if (!packageName.equals("java.lang")) {
                resultClassName = resultClassName + SUFFIX;
            }
        }
        return resultClassName ;
    }
    //获取Constructor(Parcel parcel)内部书写格式
    private static final String getInForm(TypeKind typeKind,String fieldName, String filedModelSimpleName){
        System.out.println("--------" + String.format("typekind = %s, fieldName = %s, modelName = %s", typeKind.toString(), fieldName, filedModelSimpleName));
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
        return String.format("%s = in.readParcelable(%s.class.getClassLoader());\n", fieldName, filedModelSimpleName);
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
