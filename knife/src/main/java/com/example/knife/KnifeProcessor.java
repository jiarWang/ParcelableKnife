package com.example.knife;

import com.example.ParcelKnife;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by 00382071 on 2019/1/19.
 */


@AutoService(Processor.class)
public class KnifeProcessor extends AbstractProcessor{
    private Types typeUtils;
    private Elements elementsUtils;
    private Filer filer;
    private Messager messager;
    private Map<String, FieldGroup> codeGroups;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        typeUtils = processingEnvironment.getTypeUtils();
        elementsUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(ParcelKnife.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        codeGroups = new LinkedHashMap<String, FieldGroup>();
        for (Element annotatedElement: roundEnvironment.getElementsAnnotatedWith(ParcelKnife.class)){
            if (annotatedElement.getKind() != ElementKind.CLASS){
                // TODO: 2019/1/18 异常需要完善
                throw new IllegalArgumentException("classes can be only annoteted field class");
            }
            String fileName = annotatedElement.getSimpleName().toString();
            codeGroups.put(fileName, new FieldGroup((TypeElement) annotatedElement));

        }
        for (Map.Entry<String, FieldGroup> entry : codeGroups.entrySet()){
            try {
                entry.getValue().generateCode(elementsUtils, filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
