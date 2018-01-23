package cn.xishan.oftenporter.porter.core.apt;

import cn.xishan.oftenporter.porter.core.annotation.PortInObj;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.List;

/**
 * <br>
 * Created by https://github.com/CLovinr on 2016/9/10.
 */
class SourceGenerator implements Closeable
{
    private String packageName;
    private String name;
    private Set<String> imports;
    private List<ReturnAndName> nece, unece;

    static class ReturnAndName
    {
        String returnType, methodName, var_name, setMethodName;
        Annotation annotation;

        public ReturnAndName(String returnType, String name, Annotation annotation)
        {
            this.returnType = returnType;
            this.methodName = name;
            this.annotation = annotation;
            if (name.startsWith("is"))
            {
                name = name.substring(2);

            } else if (name.startsWith("get"))
            {
                name = name.substring(3);
            }
            setMethodName = "set" + firsUpperCase(name);
            this.var_name = firsLowerCase(name);
        }

    }


    private Writer _writer;

    public SourceGenerator()
    {

    }

    public SourceGenerator append(char c) throws IOException
    {
        _writer.append(c);
        return this;
    }

    public SourceGenerator append(CharSequence c) throws IOException
    {
        _writer.append(c);
        return this;
    }

    public void init(String packageName, String name)
    {
        this.packageName = packageName;
        this.name = name;
        imports = new HashSet<>();
        nece = new ArrayList<>();
        unece = new ArrayList<>();

        imports.add(PortInObj.class.getName());
    }


    private String getReturnType(TypeMirror returnType)
    {
        TypeKind kind = returnType.getKind();
        switch (kind)
        {
            case BOOLEAN:
                return "boolean";
            case BYTE:
                return "byte";
            case SHORT:
                return "short";
            case INT:
                return "int";
            case LONG:
                return "long";
            case CHAR:
                return "char";
            case FLOAT:
                return "float";
            case DOUBLE:
                return "double";
            case ARRAY:
            {
                ArrayType at = (ArrayType) returnType;
                return getReturnType(at.getComponentType()) + "[]";
            }
            case DECLARED:
            {
                DeclaredType dt = (DeclaredType) returnType;
                TypeElement typeElement = (TypeElement) dt.asElement();
                String name = typeElement.getQualifiedName().toString();
                return name;
            }
            default:
                String err = "unknown method return type:" + returnType;
                throw new GenException(err);

        }
    }

    private ReturnAndName addImport(ExecutableElement methodElement, Annotation annotation)
    {
        String returnType = getReturnType(methodElement.getReturnType());
        int dotIndex = returnType.lastIndexOf('.');
        if (dotIndex != -1)
        {
            imports.add(returnType);
            returnType = returnType.substring(dotIndex + 1);
        }
        return new ReturnAndName(returnType, methodElement.getSimpleName().toString(), annotation);
    }

    public void addNeceMethod(ExecutableElement methodElement)
    {
        nece.add(addImport(methodElement, methodElement.getAnnotation(PortInObj.Nece.class)));
    }

    public void addUnNeceMethod(ExecutableElement methodElement)
    {
        unece.add(addImport(methodElement, methodElement.getAnnotation(PortInObj.UnNece.class)));
    }


    private SourceGenerator newLine() throws IOException
    {
        append("\n");
        return this;
    }

    private static final String PREFIX = "    ";

    private SourceGenerator prefix(int n) throws IOException
    {
        for (int i = 0; i < n; i++)
        {
            append(PREFIX);
        }
        return this;
    }


    public void setWriter(Writer writer)
    {
        this._writer = writer;
    }

    public void write() throws IOException
    {
        append("package ").append(packageName).append(';').newLine().newLine();
        Iterator<String> iterator = imports.iterator();
        while (iterator.hasNext())
        {
            String clazz = iterator.next();
            append("import ").append(clazz).append(';').newLine();
        }
        newLine();

        append("public class ").append(name).append(PorterProcessor.SUFFIX).append(" implements ").append(name)
                .newLine();
        append('{').newLine();

        String neceAnnotation = "@" + PortInObj.class.getSimpleName() + "." + PortInObj.Nece.class.getSimpleName();
        String unneceAnnotation = "@" + PortInObj.class.getSimpleName() + "." + PortInObj.UnNece.class
                .getSimpleName();
        for (int i = 0; i < nece.size(); i++)
        {
            ReturnAndName returnAndName = nece.get(i);
            writeFieldAndMethod(returnAndName, neceAnnotation);
        }

        for (int i = 0; i < unece.size(); i++)
        {
            ReturnAndName returnAndName = unece.get(i);
            writeFieldAndMethod(returnAndName, unneceAnnotation);
        }

        writeToString();
        append('}').newLine();
    }


    private static String firsLowerCase(String str)
    {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    private static String firsUpperCase(String str)
    {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private SourceGenerator writeToString() throws IOException
    {
        prefix(1).append("public String toString()").newLine();
        prefix(1).append("{").newLine();

        StringBuilder sb = new StringBuilder("\"{\"+");
        for (int i = 0; i < nece.size(); i++)
        {
            ReturnAndName item = nece.get(i);
            sb.append("\"").append(item.var_name).append("=\"+").append("this.").append(item.var_name)
                    .append("+\",\"+");
        }

        for (int i = 0; i < unece.size(); i++)
        {
            ReturnAndName item = unece.get(i);
            sb.append("\"").append(item.var_name).append("=\"+").append("this.").append(item.var_name)
                    .append("+\",\"+");
        }

        if (sb.length() > 4)
        {
            sb.delete(sb.length() - 4, sb.length());
        }
        sb.append("\"}\"");
        prefix(2).append("return ").append(sb).append(";").newLine();
        prefix(1).append("}").newLine().newLine();
        return this;
    }

    private void writeFieldAndMethod(ReturnAndName returnAndName, String annotation) throws
            IOException
    {
        String varName = returnAndName.var_name;

        //注解和变量
        prefix(1).append(annotation);
        if (returnAndName.annotation != null)
        {
            if (returnAndName.annotation instanceof PortInObj.Nece)
            {
                PortInObj.Nece nece = (PortInObj.Nece) returnAndName.annotation;
                append("(value=\"").append(nece.value()).append("\")");
            } else
            {
                PortInObj.UnNece unNece = (PortInObj.UnNece) returnAndName.annotation;
                append("(value=\"").append(unNece.value()).append("\")");
            }
        }
        newLine();

        prefix(1).append("private ").append(returnAndName.returnType).append(' ').append(varName).append(';')
                .newLine();

        //get方法
        prefix(1).append("public ").append(returnAndName.returnType).append(' ')
                .append(returnAndName.methodName)
                .append("()").newLine();
        prefix(1).append('{').newLine();
        prefix(2).append("return ").append(varName).append(';').newLine();
        prefix(1).append('}').newLine().newLine();

        //set
        prefix(1).append("public ").append(name).append(' ').append(returnAndName.setMethodName).append('(')
                .append(returnAndName.returnType)
                .append(' ')
                .append(varName).append(')').newLine();
        prefix(1).append('{').newLine();
        prefix(2).append("this.").append(varName).append("=").append(varName).append(';').newLine();
        prefix(2).append("return this;").newLine();
        prefix(1).append('}').newLine().newLine();
    }

    @Override
    public void close() throws IOException
    {
        _writer.close();
    }

}
