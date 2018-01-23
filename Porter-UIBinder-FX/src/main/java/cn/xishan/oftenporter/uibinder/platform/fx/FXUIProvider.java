package cn.xishan.oftenporter.uibinder.platform.fx;

import cn.xishan.oftenporter.uibinder.core.*;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

import java.util.HashMap;
import java.util.Set;

/**
 * <pre>
 * 支持的javafx组件包括:
 * </pre>
 *
 * @author Created by https://github.com/CLovinr on 2016/10/6.
 */
public class FXUIProvider extends UIProvider
{
    private UIAttrGetter uiAttrGetter;
    private HashMap<UiId, Binder> hashMap;
    private Parent parent;

    /**
     * @param prefix 接口参数
     */
    public FXUIProvider(Prefix prefix, Parent parent)
    {
        super(prefix);
        this.parent = parent;
    }

    public FXUIProvider(Prefix prefix, Parent parent, String idString)
    {
        this(prefix, parent);
        setIdString(idString);
    }

    @Override
    public void search(UIPlatform uiPlatform)
    {
        hashMap = new HashMap<>();
        search(uiPlatform, parent);
    }

    private void search(UIPlatform uiPlatform, Parent parent)
    {
        ObservableList<Node> list = parent.getChildrenUnmodifiable();
        if (list.size() == 0)
        {
            return;
        }
        final String idPrefix = getPrefix().idPrefix;
        final String pathPrefix = getPrefix().pathPrefix;
        IdDeal idDeal = uiPlatform.getIdDeal();
        BinderFactory factory = uiPlatform.getBinderFactory();
        for (int i = 0; i < list.size(); i++)
        {
            Node node = list.get(i);
            UiId id = UiId.newInstance(node.getId(), idPrefix);
            if (id != null)
            {
                IdDeal.Result result = idDeal.dealId(id, pathPrefix);
                if (result != null)
                {
                    hashMap.put(id, factory.getBinder(node));
                }
            }
            if (node instanceof Parent)
            {
                search(uiPlatform, (Parent) node);
            }
            if (node instanceof MenuBar)
            {
                search(uiPlatform, (MenuBar) node);
            }
        }
    }

    /**
     * 菜单栏的搜索
     *
     * @param uiPlatform
     * @param menuBar
     */
    private void search(UIPlatform uiPlatform, MenuBar menuBar)
    {
        ObservableList<Menu> list = menuBar.getMenus();
        if (list.size() == 0)
        {
            return;
        }
        final String idPrefix = getPrefix().idPrefix;
        final String pathPrefix = getPrefix().pathPrefix;
        IdDeal idDeal = uiPlatform.getIdDeal();
        BinderFactory factory = uiPlatform.getBinderFactory();
        for (int i = 0; i < list.size(); i++)
        {
            Menu menu = list.get(i);
            UiId id = UiId.newInstance(menu.getId(), idPrefix);
            if (id != null)
            {
                IdDeal.Result result = idDeal.dealId(id, pathPrefix);
                if (result != null)
                {
                    hashMap.put(id, factory.getBinder(menu));
                }
            }
            search(uiPlatform, menu);
        }
    }

    /**
     * 搜索子菜单
     *
     * @param uiPlatform
     * @param menu
     */
    private void search(UIPlatform uiPlatform, Menu menu)
    {
        ObservableList<MenuItem> list = menu.getItems();
        if (list.size() == 0)
        {
            return;
        }
        final String idPrefix = getPrefix().idPrefix;
        final String pathPrefix = getPrefix().pathPrefix;
        IdDeal idDeal = uiPlatform.getIdDeal();
        BinderFactory factory = uiPlatform.getBinderFactory();
        for (int i = 0; i < list.size(); i++)
        {
            MenuItem menuItem = list.get(i);
            UiId id = UiId.newInstance(menuItem.getId(), idPrefix);
            if (id != null)
            {
                IdDeal.Result result = idDeal.dealId(id, pathPrefix);
                if (result != null)
                {
                    hashMap.put(id, factory.getBinder(menuItem));
                }
            }
            if (menuItem instanceof Menu)
            {
                search(uiPlatform, (Menu) menuItem);
            }
        }
    }


    @Override
    public Set<UiId> getUIs()
    {
        return hashMap.keySet();
    }

    @Override
    public Binder getBinder(UiId uiId)
    {
        return hashMap.get(uiId);
    }

    @Override
    public UIAttrGetter getUIAttrGetter()
    {
        if (uiAttrGetter == null)
        {
            uiAttrGetter = new UIAttrGetterImpl();
        }
        return uiAttrGetter;
    }


}
