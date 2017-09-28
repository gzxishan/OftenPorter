package cn.xishan.oftenporter.porter.core.annotation.sth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by https://github.com/CLovinr on 2017/9/28.
 */
class AutoSetHandleWorkedInstance {

    private Map<Integer, List<Object>> worked = new HashMap<>();

    public synchronized void clear() {
        worked.clear();
    }

    public synchronized boolean workInstance(Object object) {
        boolean isWorked = false;
        if (object != null) {

            if(object.getClass().getPackage().getName().startsWith("java.")){
                return true;
            }

            List<Object> list = worked.get(object.hashCode());
            if (list == null) {
                list = new ArrayList<>();
                worked.put(object.hashCode(), list);
                list.add(object);
            } else {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) == object) {
                        isWorked = true;
                        break;
                    }
                }
                if (!isWorked) {
                    list.add(object);
                }
            }
        }
        return isWorked;
    }

}
