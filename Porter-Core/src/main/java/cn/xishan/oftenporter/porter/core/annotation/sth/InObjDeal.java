package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.annotation.PortInObj;
import cn.xishan.oftenporter.porter.core.annotation.PortInObjBind;
import cn.xishan.oftenporter.porter.core.annotation.deal.*;
import cn.xishan.oftenporter.porter.core.apt.AutoGen;
import cn.xishan.oftenporter.porter.core.apt.PorterProcessor;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.PackageUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.porter.simple.parsers.ParserUtil;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import cn.xishan.oftenporter.porter.core.base.InNames.Name;

/**
 * 处理对象绑定。
 *
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public class InObjDeal
{
    private final Logger LOGGER;
    SthUtil sthUtil;


    public InObjDeal()
    {
        LOGGER = LogUtil.logger(InObjDeal.class);
        sthUtil = new SthUtil();
    }

    /**
     * 处理接口函数上的对象绑定。
     */
    InObj dealPortInObj(PortInObjBind.ObjList objList, Method method,
            InnerContextBridge innerContextBridge, AutoSetHandle autoSetHandle) throws Exception
    {
        InObj inObj = null;
        _PortInObj portInObj = innerContextBridge.annotationDealt.portInObj(objList, method);
        if (portInObj != null)
        {
            inObj = dealPortInObj(portInObj, innerContextBridge, autoSetHandle);
        }

        return inObj;
    }

    InObj dealPortInObj(Class<?> clazz, InnerContextBridge innerContextBridge,
            AutoSetHandle autoSetHandle) throws Exception
    {
        _PortInObj portInObj = innerContextBridge.annotationDealt.portInObj(clazz);
        return dealPortInObj(portInObj, innerContextBridge, autoSetHandle);
    }

    InObj dealPortInObj(_PortInObj portInObj, InnerContextBridge innerContextBridge,
            AutoSetHandle autoSetHandle) throws Exception
    {
        CacheTool cacheTool = innerContextBridge.innerBridge.cacheTool;
        InObj inObj = null;

        if (portInObj != null)
        {

            _PortInObj.CLASS[] types = portInObj.getValue();
            One[] ones = new One[types.length];
            for (int i = 0; i < types.length; i++)
            {
                _PortInObj.CLASS clzz = types[i];
                ones[i] = bindOne(clzz.clazz, innerContextBridge);
                ones[i].setInObjClazz(clzz);
                cacheTool.put(clzz.clazz, new CacheOne(ones[i]));
                if (clzz.inObjHandle != null)
                {
                    autoSetHandle.addAutoSetsForNotPorter(new Object[]{clzz.inObjHandle});
                }
            }
            inObj = new InObj(ones);
        }
        return inObj;
    }


    One bindOne(Class<?> clazz, InnerContextBridge innerContextBridge) throws Exception
    {

        One one;
        if (Modifier.isInterface(clazz.getModifiers()))
        {
            if (clazz.isAnnotationPresent(AutoGen.class))
            {
                AutoGen autoGen = clazz.getAnnotation(AutoGen.class);
                String name;
                Class<?> key = autoGen.classValue();
                if (key.equals(AutoGen.class))
                {
                    name = autoGen.value();
                } else
                {
                    name = key.getName();
                }
                if ("".equals(name))
                {
                    clazz = PackageUtil
                            .newClass(clazz.getName() + PorterProcessor.SUFFIX, innerContextBridge.classLoader);
                } else
                {
                    Class<?> c = innerContextBridge.autoGenImplMap.get(name);
                    if (c == null)
                    {
                        throw new RuntimeException("not find interface implementation of " + clazz);
                    } else
                    {
                        clazz = c;
                    }
                }
            } else
            {
                throw new RuntimeException("interface have to be with annotation @" + AutoGen.class.getSimpleName());
            }
        } else if (Modifier.isAbstract(clazz.getModifiers()))
        {//若是接口，执行此句也会为true。
            throw new RuntimeException("abstract class is not supported!(" + clazz + ")");
        }


        BackableSeek backableSeek = new BackableSeek();
        backableSeek.push();
        //绑定类型转换。
        sthUtil.bindParserAndParse(clazz, innerContextBridge, null, backableSeek, true);

        Field[] fields = WPTool.getAllFields(clazz);
        List<Field> neces = new ArrayList<>();
        List<Name> neceNames = new ArrayList<>();
        List<_Nece> neceDeals = new ArrayList<>();
        List<Field> unneces = new ArrayList<>();
        List<Name> unneceNames = new ArrayList<>();

        List<Field> jsonObjFields = new ArrayList<>();
        List<One> jsonObjOnes = new ArrayList<>();
        List<String> jsonObjVarnames = new ArrayList<>();

        AnnotationDealt annotationDealt = innerContextBridge.annotationDealt;
        TypeParserStore typeParserStore = innerContextBridge.innerBridge.globalParserStore;

        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];

            PortInObj.JsonObj jsonObj = field.getAnnotation(PortInObj.JsonObj.class);
            if (jsonObj != null && jsonObj.willSetForRequest())
            {
                CacheOne cacheOne = innerContextBridge.innerBridge.cacheTool
                        .getCacheOne(field.getType(), innerContextBridge);
                jsonObjFields.add(field);
                jsonObjOnes.add(cacheOne.getOne());
                String varName = (jsonObj.value().equals("") ? field.getName() : jsonObj
                        .value());
                jsonObjVarnames.add(varName);
                field.setAccessible(true);
                continue;
            }

            Name name;
            field.setAccessible(true);
            List<Name> nameList = null;
            List<Field> fieldList = null;
            String nameStr = null;
            _Nece nece = annotationDealt.nece(field);
            _UnNece unNece;
            if (nece != null)
            {
                nameStr = nece.getValue();
                nameList = neceNames;
                neceDeals.add(nece);
                fieldList = neces;
            } else if ((unNece = annotationDealt.unNece(field)) != null)
            {
                nameStr = unNece.getValue();
                nameList = unneceNames;
                fieldList = unneces;
            }

            if (nameList != null)
            {
                name = new Name(nameStr, backableSeek.getTypeId(nameStr));
                _parse parse = annotationDealt.parse(field);
                if (parse != null)
                {
                    InNames temp = InNames.temp(name);
                    SthUtil.bindTypeParser(temp, parse, typeParserStore, backableSeek,
                            BackableSeek.SeekType.NotAdd_Bind);
                }

                if (name.typeParserId == null)
                {
                    Class<? extends ITypeParser> typeParser;
                    LOGGER.debug("{} for field [{}] is null,now try auto...", ITypeParser.class.getSimpleName(), field);
                    try
                    {
                        typeParser = ParserUtil.getTypeParser(field.getType());
                        LOGGER.debug("auto get:[{}]", typeParser);
                        String typeId = SthUtil.putTypeParser(typeParser, typeParserStore);
                        name = new Name(nameStr, typeId);
                    } catch (ClassNotFoundException e)
                    {
                        LOGGER.warn("auto get {} for field '{}' failed!", ITypeParser.class.getSimpleName(), field);
                        throw e;
                    }

                }
                nameList.add(name);
                fieldList.add(field);
            }

        }
        one = new One(clazz,
                new InNames(neceNames.toArray(new Name[0]), neceDeals.toArray(new _Nece[0]),
                        unneceNames.toArray(new Name[0]), null),
                neces.toArray(new Field[0]), unneces.toArray(new Field[0]), jsonObjFields.toArray(new Field[0]),
                jsonObjOnes.toArray(new One[0]), jsonObjVarnames.toArray(new String[0]));


        //       CacheOne cacheOne = new CacheOne(one);
//        //获取父类的绑定。
//        BindFromSuperUtil.bindFromSuperClass(clazz, cacheOne);

        return one;
    }


}
