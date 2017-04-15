package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.base.ParamSource;
import cn.xishan.oftenporter.porter.core.base.ParamSourceHandle;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.KeyUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
import cn.xishan.oftenporter.porter.simple.DefaultParamsSource;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/15.
 */
class MultiPartParamSourceHandle implements ParamSourceHandle
{
    private MultiPartOption multiPartOption;
    private DiskFileItemFactory factory;

    public MultiPartParamSourceHandle(MultiPartOption multiPartOption)
    {
        this.multiPartOption = multiPartOption;
        factory = new DiskFileItemFactory();
        if (multiPartOption.tempDir != null)
        {
            factory.setRepository(new File(multiPartOption.tempDir));
        }
        factory.setSizeThreshold(multiPartOption.cacheSize);
    }

    @Override
    public ParamSource get(WObject wObject, Class<?> porterClass, Method porterFun) throws Exception
    {
        HttpServletRequest request = wObject.getRequest().getOriginalRequest();
        if (!ServletFileUpload.isMultipartContent(request))
        {
            return null;
        }

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(multiPartOption.maxBytesEachFile);
        upload.setSizeMax(multiPartOption.totalBytes);
        Map<String, Object> map = new HashMap<>();
        List<FileItem> list = upload.parseRequest(request);
        for (FileItem fileItem : list)
        {
            String name = fileItem.getFieldName();
            Object value;
            if (!fileItem.isFormField() && fileItem.isInMemory())
            {
                File tempFile;
                if (fileItem.isInMemory())
                {
                    tempFile = File.createTempFile(KeyUtil.randomUUID(), ".temp", new File(multiPartOption.tempDir));
                    FileTool.write2File(fileItem.getInputStream(), tempFile, true);
                } else
                {
                    DiskFileItem diskFileItem = (DiskFileItem) fileItem;
                    tempFile = diskFileItem.getStoreLocation();
                }

                String origin = fileItem.getName();
                if (WPTool.isEmpty(origin))
                {
                    origin = name;
                }
                value = new FilePart(origin, tempFile);
            } else
            {
                value = fileItem.getString();
            }
            map.put(name, value);
        }

        ParamSource paramSource = new DefaultParamsSource(map, wObject.getRequest());

        return paramSource;
    }
}
