package org.joget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.map.ListOrderedMap;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListBinder;
import org.joget.apps.datalist.model.DataListFilter;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.PluginManager;

public class HyperlinkOptionsFilter extends DataListFilterTypeDefault {
    private final static String MESSAGE_PATH = "message/HyperlinkOptionsFilter";
    
    public String getName() {
        return "Hyperlink Options Filter Type";
    }

    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getLabel() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.HyperlinkOptionsFilter.pluginLabel", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getDescription() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.HyperlinkOptionsFilter.pluginDesc", getClassName(), MESSAGE_PATH);
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/hyperlinkOptionsFilter.json", null, true, MESSAGE_PATH);
    }

    public String getTemplate(DataList datalist, String name, String label) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        Map dataModel = new HashMap();
        
        dataModel.put("element", this);
        dataModel.put("name", datalist.getDataListEncodedParamName(DataList.PARAMETER_FILTER_PREFIX+name));
        dataModel.put("label", label);
        
        Map<String, String> options = getOptionMap();
        if ("true".equalsIgnoreCase(getPropertyString("showCount"))) {
            DataListBinder binder = datalist.getBinder();
            for (String key : options.keySet()) {
                DataListFilterQueryObject filter = getQueryObject(datalist, name, key);
                int count = 0;
                if (binder != null) {
                    if (filter != null) {
                        count = binder.getDataTotalRowCount(datalist, binder.getProperties(), new DataListFilterQueryObject[]{filter});
                    } else {
                        count = binder.getDataTotalRowCount(datalist, binder.getProperties(), new DataListFilterQueryObject[]{});
                    }
                }
                
                options.put(key, options.get(key) + " (" + count + ")");
            }
        }
        
        String value = getValue(datalist, name, getPropertyString("defaultValue"));
        dataModel.put("value", value);
        dataModel.put("options", options);
            
        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/hyperlinkOptionsFilter.ftl", null);
    }
    
    protected Map<String, String> getOptionMap() {
        Map<String, String> optionMap = new ListOrderedMap();
        
        // load from "options" property
        Object[] options = (Object[]) getProperty(FormUtil.PROPERTY_OPTIONS);
        for (Object o : options) {
            Map option = (HashMap) o;
            Object value = option.get(FormUtil.PROPERTY_VALUE);
            Object label = option.get(FormUtil.PROPERTY_LABEL);
            if (value != null && label != null) {
                optionMap.put(value.toString(), label.toString());
            }
        }

        // load from binder if available
        Map optionsBinderProperties = (Map) getProperty("optionsBinder");
        if (optionsBinderProperties != null && optionsBinderProperties.get("className") != null && !optionsBinderProperties.get("className").toString().isEmpty()) {
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            FormBinder optionBinder = (FormBinder) pluginManager.getPlugin(optionsBinderProperties.get("className").toString());
            if (optionBinder != null) {
                optionBinder.setProperties((Map) optionsBinderProperties.get("properties"));
                FormRowSet rowSet = ((FormLoadBinder) optionBinder).load(null, null, null);
                if (rowSet != null) {
                    optionMap = new ListOrderedMap();
                    for (FormRow row : rowSet) {
                        Iterator<String> it = row.stringPropertyNames().iterator();
                        // get the key based on the "value" property
                        String value = row.getProperty(FormUtil.PROPERTY_VALUE);
                        if (value == null) {
                            // no "value" property, use first property instead
                            String key = it.next();
                            value = row.getProperty(key);
                        }
                        // get the label based on the "label" property
                        String label = row.getProperty(FormUtil.PROPERTY_LABEL);
                        if (label == null) {
                            // no "label" property, use next property instead
                            String key = it.next();
                            label = row.getProperty(key);
                        }
                        optionMap.put(value, label);
                    }
                }
            }
        }
        
        if (!optionMap.containsKey("")) {
            Map<String, String> tempOptionMap = new ListOrderedMap();
            tempOptionMap.put("", AppPluginUtil.getMessage("HyperlinkOptionsFilter.all", getClassName(), MESSAGE_PATH));
            tempOptionMap.putAll(optionMap);
            optionMap = tempOptionMap;
        }
        
        return optionMap;
    }
    
    protected DataListFilterQueryObject getQueryObject(DataList datalist, String name, String value) {
        DataListFilterQueryObject queryObject = new DataListFilterQueryObject();
        if (datalist != null && datalist.getBinder() != null && value != null && !value.isEmpty()) {
            String columnName = datalist.getBinder().getColumnName(name);
            List<String> valuesList = new ArrayList<String>();

            String query = "("+columnName+" = ? or "+columnName+" like ? or "+columnName+" like ? or "+columnName+" like ?)";
            valuesList.add(value);
            valuesList.add(value + ";%");
            valuesList.add("%;" + value + ";%");
            valuesList.add("%;" + value);

            queryObject.setOperator(DataListFilter.OPERATOR_AND);
            queryObject.setQuery(query);
            queryObject.setValues(valuesList.toArray(new String[0]));

            return queryObject;
        }
        return null;
    }

    public DataListFilterQueryObject getQueryObject(DataList datalist, String name) {
        String value = getValue(datalist, name, getPropertyString("defaultValue"));
        return getQueryObject(datalist, name, value);
    }
}
