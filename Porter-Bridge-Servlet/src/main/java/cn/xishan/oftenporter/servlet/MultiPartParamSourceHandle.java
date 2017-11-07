package cn.xishan.oftenporter.servlet;

import cn.xishan.oftenporter.porter.core.base.ParamSource;
import cn.xishan.oftenporter.porter.core.base.PortMethod;
import cn.xishan.oftenporter.porter.core.base.WObject;
import cn.xishan.oftenporter.porter.core.util.FileTool;
import cn.xishan.oftenporter.porter.core.util.KeyUtil;
import cn.xishan.oftenporter.porter.core.util.WPTool;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by https://github.com/CLovinr on 2017/4/15.
 */
class MultiPartParamSourceHandle extends PutParamSourceHandle {
    private MultiPartOption multiPartOption;
    private DiskFileItemFactory factory;
    private boolean dealNormalPut;

    public MultiPartParamSourceHandle(MultiPartOption multiPartOption, boolean dealNormalPut) {
        this.multiPartOption = multiPartOption;
        this.dealNormalPut = dealNormalPut;
        factory = new DiskFileItemFactory();
        if (multiPartOption.tempDir != null) {
            factory.setRepository(new File(multiPartOption.tempDir));
        }
        factory.setSizeThreshold(multiPartOption.cacheSize);
    }

    @Override
    public ParamSource get(WObject wObject, Class<?> porterClass, Method porterFun) throws Exception {
        ParamSource paramSource = _get(wObject, porterClass, porterFun);
        if (paramSource == null && dealNormalPut && wObject.getRequest().getMethod() == PortMethod.PUT) {
            paramSource = super.get(wObject, porterClass, porterFun);
        }
        return paramSource;
    }

    ParamSource _get(WObject wObject, Class<?> porterClass, Method porterFun) throws Exception {


        Object originalRequest = wObject.getRequest().getOriginalRequest();
        if (originalRequest == null || !(originalRequest instanceof HttpServletRequest)) {
            return null;
        }
        HttpServletRequest request = (HttpServletRequest) originalRequest;
        if (ContentType.APP_JSON.name().equals(request.getContentType())) {
            if (multiPartOption.decodeJsonParams) {
                JSONObject jsonObject = JSON.parseObject(
                        FileTool.getString(request.getInputStream(), 1024, request.getCharacterEncoding()));
                ParamSource paramSource = new DefaultParamSource(jsonObject, wObject.getRequest());
                return paramSource;
            } else {
                return null;
            }
        } else if (!ServletFileUpload.isMultipartContent(request)) {
            return null;
        }

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(multiPartOption.maxBytesEachFile);
        upload.setSizeMax(multiPartOption.totalBytes);
        Map<String, Object> map = new HashMap<>();
        List<FileItem> list = upload.parseRequest(request);
        for (FileItem fileItem : list) {
            String name = fileItem.getFieldName();
            Object value;
            if (fileItem.isFormField()) {
                String encoding = request.getCharacterEncoding();
                if(WPTool.isEmpty(encoding)){
                    encoding="utf-8";
                }
                value = fileItem.getString(encoding);
            } else {
                File tempFile;
                if (fileItem.isInMemory()) {
                    tempFile = File.createTempFile(KeyUtil.randomUUID(), ".temp", new File(multiPartOption.tempDir));
                    FileTool.write2File(fileItem.getInputStream(), tempFile, true);
                } else {
                    DiskFileItem diskFileItem = (DiskFileItem) fileItem;
                    tempFile = diskFileItem.getStoreLocation();
                }

                String origin = fileItem.getName();
                if (WPTool.isEmpty(origin)) {
                    origin = name;
                }
                value = new FilePart(origin, tempFile);

            }
            map.put(name, value);
        }

        ParamSource paramSource = new DefaultParamSource(map, wObject.getRequest());

        return paramSource;
    }
}
