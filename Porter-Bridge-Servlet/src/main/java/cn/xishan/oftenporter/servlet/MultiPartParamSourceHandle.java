package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.base.ParamSource;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.OftenObject;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.OftenKeyUtil;
import cn.xishan.oftenporter.porter.core.util.OftenTool;
import cn.xishan.oftenporter.porter.simple.DefaultParamSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 另见{@linkplain MultiPartOption}。
 *
 * @author Created by https://github.com/CLovinr on 2017/4/15.
 */
class MultiPartParamSourceHandle extends PutParamSourceHandle
{
    private MultiPartOption multiPartOption;
    private DiskFileItemFactory factory;
    private boolean dealNormalPut;

    public MultiPartParamSourceHandle(MultiPartOption multiPartOption, boolean dealNormalPut)
    {
        this.multiPartOption = multiPartOption;
        this.dealNormalPut = dealNormalPut;
        factory = new DiskFileItemFactory();
        if (multiPartOption.tempDir != null)
        {
            factory.setRepository(new File(multiPartOption.tempDir));
        }
        factory.setSizeThreshold(multiPartOption.cacheSize);
    }

    @Override
    public ParamSource get(OftenObject oftenObject, Class<?> porterClass, Method porterFun) throws Exception
    {
        ParamSource paramSource = _get(oftenObject, porterClass, porterFun);
        if (paramSource == null && dealNormalPut && oftenObject.getRequest().getMethod() == PortMethod.PUT)
        {
            paramSource = super.get(oftenObject, porterClass, porterFun);
        }
        return paramSource;
    }

    ParamSource _get(OftenObject oftenObject, Class<?> porterClass, Method porterFun) throws Exception
    {
        Object originalRequest = oftenObject.getRequest().getOriginalRequest();
        if (!(originalRequest instanceof HttpServletRequest))
        {
            return null;
        }
        HttpServletRequest request = (HttpServletRequest) originalRequest;
        if (ContentType.APP_JSON.name().equals(request.getContentType()))
        {
            if (multiPartOption.decodeJsonParams)
            {
                JSONObject jsonObject = JSON.parseObject(
                        FileTool.getString(request.getInputStream(), 1024, request.getCharacterEncoding()));
                ParamSource paramSource = new DefaultParamSource(jsonObject, oftenObject.getRequest());
                return paramSource;
            } else
            {
                return null;
            }
        } else if (!ServletFileUpload.isMultipartContent(request) ||
                porterFun.isAnnotationPresent(IgnoreDefaultMultipart.class) ||
                porterClass.isAnnotationPresent(IgnoreDefaultMultipart.class)
        )
        {
            return null;
        }

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(multiPartOption.maxBytesEachFile);
        upload.setSizeMax(multiPartOption.totalBytes);
        Map<String, Object> map = new HashMap<>();
        Map<String, List<Object[]>> filesMap = new HashMap<>(2);

        List<FileItem> list = upload.parseRequest(request);
        for (FileItem fileItem : list)
        {
            String name = fileItem.getFieldName();
            Object value;
            if (fileItem.isFormField())
            {
                String encoding = request.getCharacterEncoding();
                if (OftenTool.isEmpty(encoding))
                {
                    encoding = "utf-8";
                }
                value = fileItem.getString(encoding);
                map.put(name, value);
            } else
            {
                File tempFile;
                if (fileItem.isInMemory())
                {
                    tempFile = new File(multiPartOption.tempDir + "/" + OftenKeyUtil.randomUUID() + ".tmp");
                    FileTool.write2File(fileItem.getInputStream(), tempFile, true);
                } else
                {
                    DiskFileItem diskFileItem = (DiskFileItem) fileItem;
                    tempFile = diskFileItem.getStoreLocation();
                }

                String origin = fileItem.getName();
                if (OftenTool.isEmpty(origin))
                {
                    origin = OftenKeyUtil.randomUUID();
                }

                List<Object[]> files = filesMap.computeIfAbsent(name, k -> new ArrayList<>(2));
                files.add(new Object[]{origin, tempFile, fileItem});//引用fileItem，防止文件被清理
            }
        }

        for (Map.Entry<String, List<Object[]>> entry : filesMap.entrySet())
        {
            List<Object[]> filesList = entry.getValue();
            String[] origins = new String[filesList.size()];
            File[] files = new File[filesList.size()];
            FileItem[] fileItems = new FileItem[filesList.size()];

            for (int i = 0; i < origins.length; i++)
            {
                Object[] objs = filesList.get(i);
                origins[i] = (String) objs[0];
                files[i] = (File) objs[1];
                fileItems[i] = (FileItem) objs[2];
            }
            //引用fileItem，防止文件使用前就被清理
            map.put(entry.getKey(), new FilePart(origins, files, fileItems));
        }

        ParamSource paramSource = new DefaultParamSource(map, oftenObject.getRequest());

        return paramSource;
    }
}
