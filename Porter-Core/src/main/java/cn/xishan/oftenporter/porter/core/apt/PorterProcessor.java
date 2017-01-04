package cn.xishan.oftenporter.porter.core.apt;

import cn.xishan.oftenporter.porter.core.annotation.PortInObj;
import cn.xishan.oftenporter.porter.core.util.WPTool;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by https://github.com/CLovinr on 2016/9/8.
 */
@SupportedAnnotationTypes({"cn.xishan.oftenporter.porter.core.apt.AutoGen"})
public class PorterProcessor extends AbstractProcessor
{
    public static final String SUFFIX = "AP";
    private ProcessingEnvironment processingEnv;

    private void err(String msg, Element element)
    {
        processingEnv.getMessager()
                .printMessage(Diagnostic.Kind.ERROR, msg, element);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        for (TypeElement element : annotatedElementsIn(roundEnv))
        {
            genCode(element);
        }
        return false;
    }

    private void genCode(TypeElement element)
    {
        Closeable closeable = null;
        try
        {

            PackageElement packageElement =
                    (PackageElement) element.getEnclosingElement();
            SourceGenerator sourceGenerator = new SourceGenerator();
            sourceGenerator.init(packageElement.getQualifiedName().toString(),
                    element.getSimpleName().toString());

            List<? extends Element> list = element.getEnclosedElements();
            for (int k = 0; k < list.size(); k++)
            {
                Element el = list.get(k);
                if (el.getKind() == ElementKind.METHOD)
                {
                    ExecutableElement executableElement = (ExecutableElement) el;
                    if (!executableElement.getModifiers().contains(Modifier.ABSTRACT))
                    {
                        continue;
                    }
                    if (isNece(executableElement))
                    {
                        sourceGenerator.addNeceMethod(executableElement);
                    } else
                    {
                        sourceGenerator.addUnNeceMethod(executableElement);
                    }
                }
            }
            String name = element.getQualifiedName() + SUFFIX;

            //创建java源文件
            Filer filer = processingEnv.getFiler();
            sourceGenerator.setWriter(filer.createSourceFile(name,element).openWriter());
            closeable = sourceGenerator;

            sourceGenerator.append("/*" + new Date() + "*/\n");
            sourceGenerator.write();

        } catch (Throwable e)
        {
            err("ex:create source file failed!\n" + e.toString(), element);
            e.printStackTrace();
        } finally
        {
            WPTool.close(closeable);
        }
    }


    private boolean isNece(ExecutableElement element)
    {
        boolean isNece = true;

        if (element.getAnnotation(PortInObj.UnNece.class) != null)
        {
            isNece = false;
        }

        return isNece;
    }

    private Set<? extends TypeElement> annotatedElementsIn(
            RoundEnvironment roundEnv)
    {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(AutoGen.class);
        Set<TypeElement> set = new HashSet<>();
        Iterator<? extends Element> it = elements.iterator();
        while (it.hasNext())
        {
            Element element = it.next();
            if (!needParse(element))
            {
                processingEnv.getMessager()
                        .printMessage(Diagnostic.Kind.WARNING, "will not generator source.", element);
                continue;
            }

            if (element.getKind() == ElementKind.INTERFACE)
            {
                set.add(TypeElement.class.cast(element));
            } else
            {
                err("just for interface!(current:" + element + ")", element);
            }
        }
        return set;
    }

    private boolean needParse(Element element)
    {
        List<? extends AnnotationMirror> list = element.getAnnotationMirrors();
        AnnotationMirror anno = null;
        for (int i = 0; i < list.size(); i++)
        {
            AnnotationMirror am = list.get(i);
            TypeElement typeElement = (TypeElement) am.getAnnotationType().asElement();
            if (typeElement.getQualifiedName().contentEquals(AutoGen.class.getName()))
            {
                anno = am;
                break;
            }
        }
        boolean needParse = true;
        if (anno != null)
        {

            Map<? extends ExecutableElement, ? extends AnnotationValue> values = anno.getElementValues();
            Iterator<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> it = values.entrySet()
                    .iterator();
            while (it.hasNext())
            {
                Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry = it.next();
                TypeElement type = (TypeElement) ((DeclaredType) (entry.getKey().getReturnType())).asElement();
                if (type.getQualifiedName().contentEquals(String.class.getName()))
                {
                    needParse = "".equals(entry.getValue());
                } else if (type.getQualifiedName().contentEquals(Class.class.getName()))
                {
                    needParse = entry.getValue().equals(AutoGen.class);
                }

                if (!needParse)
                {
                    break;
                }

            }
        }
        return needParse;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        Set<String> set = new HashSet<>(1);
        set.add(AutoGen.class.getName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latestSupported();
    }
}
