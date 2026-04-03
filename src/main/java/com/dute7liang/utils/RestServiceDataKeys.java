package com.dute7liang.utils;

import com.intellij.openapi.actionSystem.DataKey;
import com.dute7liang.restful.navigation.action.RestServiceItem;

import java.util.List;

public class RestServiceDataKeys {

  public static final DataKey<List<RestServiceItem>> SERVICE_ITEMS = DataKey.create("SERVICE_ITEMS");

  private RestServiceDataKeys() {
  }
}
