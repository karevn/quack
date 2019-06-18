package com.quack.launcher;

import com.quack.beans.LauncherConfig;
import com.quack.beans.LauncherConfigDescriptor;
import com.quack.beans.LauncherConfigDescriptorItem;
import ru.greatbit.plow.contract.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;


public abstract class BaseLauncher<Config> implements Launcher {
    private Class<Config> configType;
    private LauncherConfigDescriptor configDescriptor;

    @SuppressWarnings("unchecked")
    public BaseLauncher() {
        this.configType = (Class<Config>)
                ((ParameterizedType) getClass()
                        .getGenericSuperclass())
                        .getActualTypeArguments()[0];
    }

    protected synchronized LauncherConfigDescriptor createConfigDescriptor() {
        if (configDescriptor != null) {
            return configDescriptor;
        }
        configDescriptor = new LauncherConfigDescriptor()
                .withLauncherId(getLauncherId()).withName(getLauncherName());
        for (Field field : configType.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigItem.class)) {
                ConfigItem annotation = field.getAnnotation(ConfigItem.class);
                configDescriptor.getConfigDescriptors().add(new LauncherConfigDescriptorItem(
                        field.getName(), annotation.title(), Arrays.asList(annotation.defaultValues()), annotation.restricted(), annotation.multi()
                ));
            }
        }
        return configDescriptor;
    }

    @Override
    public LauncherConfigDescriptor getConfigDescriptor() {
        if (configDescriptor != null) {
            return configDescriptor;
        }
        return createConfigDescriptor();
    }

    @Override
    public Config getPluginConfig(Object destination, LauncherConfig config) {
        config.getProperties().forEach(configItem -> {
            Field field;
            try {
                field = destination.getClass().getDeclaredField(configItem.getKey());

                if (field.getType().isAssignableFrom(String.class)) {
                    setField(destination, field, configItem.getValue());
                }
                if (field.getType().isAssignableFrom(Long.class) || field.getType().equals(long.class)) {
                    setField(destination, field, Long.parseLong(configItem.getValue()));
                }
                if (field.getType().isAssignableFrom(Integer.class) || field.getType().equals(int.class)) {
                    setField(destination, field, Integer.parseInt(configItem.getValue()));
                }
                if (field.getType().isAssignableFrom(Float.class) || field.getType().equals(float.class)) {
                    setField(destination, field, Float.parseFloat(configItem.getValue()));
                }
                if (field.getType().isAssignableFrom(Boolean.class) || field.getType().equals(boolean.class)) {
                    setField(destination, field, (configItem.getValue() != null && configItem.getValue().toLowerCase().equals("true")));
                }

            } catch (NoSuchFieldException e) {

            } catch (IllegalAccessException e) {

            }
        });
        return (Config) destination;
    }

    private void setField(Object destination, Field field, Object value) throws IllegalAccessException {
        boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        field.set(destination, value);
        field.setAccessible(isAccessible);
    }

    @Override
    public String getLauncherId() {
        return getClass().getAnnotation(Plugin.class).name();
    }

    @Override
    public String getLauncherName() {
        return getClass().getAnnotation(Plugin.class).title();
    }

    @Override
    public boolean isToCreateLaunch() {
        return false;
    }

}
