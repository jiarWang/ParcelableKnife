package com.example.knife;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Type;
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
    private static final String SUFFIX = "Model";
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
        }
        TypeSpec codeJava = TypeSpec.classBuilder(knifeClassSimpleName)
                .addModifiers(Modifier.PUBLIC)
                .addFields(fieldSpecs)
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
        System.out.println(">>>>>>>>>>" + canonicalName);
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
            resultClassName = ClassName.get("android.os", "Parcelable");
        }
        return resultClassName;
    }
}
