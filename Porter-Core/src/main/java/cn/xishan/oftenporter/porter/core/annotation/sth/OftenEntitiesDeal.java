package cn.xishan.oftenporter.porter.core.annotation.sth;

import cn.xishan.oftenporter.porter.core.advanced.BackableSeek;
import cn.xishan.oftenporter.porter.core.advanced.ITypeParser;
import cn.xishan.oftenporter.porter.core.advanced.TypeParserStore;
import cn.xishan.oftenporter.porter.core.annotation.deal.*;
import cn.xishan.oftenporter.porter.core.annotation.param.JsonObj;
import cn.xishan.oftenporter.porter.core.base.*;
import cn.xishan.oftenporter.porter.core.init.InnerContextBridge;
import cn.xishan.oftenporter.porter.core.util.LogUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.parsers.ParserUtil;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.xishan.oftenporter.porter.core.base.InNames.Name;

/**
 * 处理对象绑定。
 *
 * @author Created by https://github.com/CLovinr on 2016/9/27.
 */
public class OftenEntitiesDeal
{
    private final Logger LOGGER;
    SthUtil sthUtil;


    public OftenEntitiesDeal()
    {
        LOGGER = LogUtil.logger(OftenEntitiesDeal.class);
        sthUtil = new SthUtil();
    }

    /**
     * 处理接口函数上的对象绑定。
     */
    OftenEntities dealOftenEntities(Class<?> porterClass, Method method,
            InnerContextBridge innerContextBridge, AutoSetHandle autoSetHandle) throws Exception
    {
        OftenEntities entities = null;
        _BindEntities bindEntities = innerContextBridge.annotationDealt.bindEntities(porterClass, method);
        if (bindEntities != null)
        {
            entities = dealOftenEntities(bindEntities, innerContextBridge, autoSetHandle);
        }

        return entities;
    }

    OftenEntities dealOftenEntities(Class<?> clazz, InnerContextBridge innerContextBridge,
            AutoSetHandle autoSetHandle) throws Exception
    {
        _BindEntities bindEntities = innerContextBridge.annotationDealt.bindEntities(clazz);
        return dealOftenEntities(bindEntities, innerContextBridge, autoSetHandle);
    }

    One dealOPEntity(Class<?> entityClass, Method method, InnerContextBridge innerContextBridge,
            AutoSetHandle autoSetHandle) throws Exception
    {
        _BindEntities bindEntities = innerContextBridge.annotationDealt.bindEntity(entityClass, method);
        OftenEntities oftenEntities = dealOftenEntities(bindEntities, innerContextBridge, autoSetHandle);
        return oftenEntities.ones[0];
    }

    private OftenEntities dealOftenEntities(_BindEntities bindEntities, InnerContextBridge innerContextBridge,
            AutoSetHandle autoSetHandle) throws Exception
    {
        CacheTool cacheTool = innerContextBridge.innerBridge.cacheTool;
        OftenEntities OftenEntities = null;

        if (bindEntities != null)
        {

            _BindEntities.CLASS[] types = bindEntities.getValue();
            One[] ones = new One[types.length];
            for (int i = 0; i < types.length; i++)
            {
                _BindEntities.CLASS clzz = types[i];
                ones[i] = bindOne(clzz.clazz, innerContextBridge, true);
                ones[i].setEntityClazz(clzz);
                cacheTool.put(clzz.clazz, new CacheOne(ones[i]));
                if (clzz.bindEntityDealtHandle != null)
                {
                    autoSetHandle.addAutoSetsForNotPorter(new Object[]{clzz.bindEntityDealtHandle});
                }
            }
            OftenEntities = new OftenEntities(ones);
        }
        return OftenEntities;
    }


    One bindOne(Class<?> clazz, InnerContextBridge innerContextBridge,
            boolean notFoundTypeParserThrows) throws Exception
    {

        One one;
        if (Modifier.isInterface(clazz.getModifiers()) || Modifier.isAbstract(clazz.getModifiers()))
        {//若是接口，执行此句也会为true。
            throw new RuntimeException("abstract class is not supported!(" + clazz + ")");
        }


        BackableSeek backableSeek = new BackableSeek();
        backableSeek.push();
        //绑定类型转换。
        sthUtil.bindParses(clazz, innerContextBridge, null, backableSeek, true, Collections.emptyMap());

        Field[] fields = OftenTool.getAllFields(clazz);
        List<Field> neces = new ArrayList<>();
        List<Name> neceNames = new ArrayList<>();
        // List<_Nece> neceDeals = new ArrayList<>();
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

            JsonObj jsonObj = AnnoUtil.getAnnotation(field, JsonObj.class);
            if (jsonObj != null && jsonObj.willSetForRequest())
            {
                CacheOne cacheOne = innerContextBridge.innerBridge.cacheTool
                        .getCacheOne(field.getType(), innerContextBridge, notFoundTypeParserThrows);
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
            _Unece unNece;
            _NeceUnece neceUnece = null;
            if (nece != null)
            {
                nameStr = nece.getVarName();
                nameList = neceNames;
                fieldList = neces;
                neceUnece = nece;
            } else if ((unNece = annotationDealt.unNece(field)) != null)
            {
                nameStr = unNece.getVarName();
                nameList = unneceNames;
                fieldList = unneces;
                neceUnece = unNece;
            }

            if (neceUnece != null)
            {
                name = new Name(nameStr, backableSeek);
                _Parse[] parses = annotationDealt.parses(field);
                if (parses.length > 0)
                {
                    InNames temp = InNames.temp(name);
                    SthUtil.bindTypeParses(temp, parses, typeParserStore, backableSeek,
                            BackableSeek.SeekType.NotAdd_Bind);
                }

                if (name.typeParserId == null)
                {
                    Class<? extends ITypeParser> typeParser;
                    LOGGER.debug("{} for field [{}] is null,now try auto...", ITypeParser.class.getSimpleName(), field);
                    try
                    {
                        typeParser = ParserUtil.getTypeParser(field.getType(), notFoundTypeParserThrows);
                        LOGGER.debug("auto get:[{}]", typeParser);
                        String typeId = SthUtil.putTypeParser(typeParser, typeParserStore);
                        name = new Name(nameStr, typeId);
                    } catch (ClassNotFoundException e)
                    {
                        LOGGER.warn("auto get {} for field '{}' failed!", ITypeParser.class.getSimpleName(), field);
                        throw e;
                    }

                }
                name.setType(annotationDealt, AnnoUtil.Advance.getRealTypeOfField(clazz, field), neceUnece);
                nameList.add(name);
                fieldList.add(field);
            }

        }
        one = new One(clazz, new InNames(neceNames.toArray(new Name[0]), unneceNames.toArray(new Name[0]), null),
                neces.toArray(new Field[0]), unneces.toArray(new Field[0]), jsonObjFields.toArray(new Field[0]),
                jsonObjOnes.toArray(new One[0]), jsonObjVarnames.toArray(OftenTool.EMPTY_STRING_ARRAY));

        return one;
    }

    public static Name getName(AnnotationDealt annotationDealt, _NeceUnece neceUnece, String nameStr, Class<?> type,
            TypeParserStore typeParserStore,
            boolean notFoundThrows) throws ClassNotFoundException
    {
        Class<? extends ITypeParser> typeParser = ParserUtil.getTypeParser(type, notFoundThrows);
        String typeId = null;
        if (typeParser != null)
        {
            typeId = SthUtil.putTypeParser(typeParser, typeParserStore);
        }
        Name name = new Name(nameStr, typeId);
        name.setType(annotationDealt, type, neceUnece);
        return name;
    }


}
