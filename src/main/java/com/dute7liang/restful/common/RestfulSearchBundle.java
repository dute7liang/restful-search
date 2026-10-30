package com.dute7liang.restful.common;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * 读取插件自有文案，跟随 IntelliJ IDEA 的界面语言选择翻译。
 */
public final class RestfulSearchBundle {
    @NonNls
    private static final String BUNDLE = "messages.RestfulSearchBundle";
    private static final DynamicBundle INSTANCE = new DynamicBundle(RestfulSearchBundle.class, BUNDLE);

    private RestfulSearchBundle() {
    }

    public static @NotNull @Nls String message(
            @NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }
}
