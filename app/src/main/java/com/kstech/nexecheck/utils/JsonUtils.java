package com.kstech.nexecheck.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

	public static <T extends Object> Object fromJson(String json, Class<T> clazz) {
		Object object = null;
		try {
			Gson gson = new Gson();
			object = gson.fromJson(json, clazz);
		} catch (JsonSyntaxException e) {
		}
		return object;
	}

	public static <T extends Object> List<T> fromArrayJson(String json, Class<T> clazz) {
		List<T> objects = new ArrayList<T>();
		T object = null;
		try {
			Gson gson = new Gson();

			JsonParser parser = new JsonParser();
			JsonArray jsonArray = parser.parse(json).getAsJsonArray();
			for (JsonElement jsonElement : jsonArray) {
				object = gson.fromJson(jsonElement, clazz);
				objects.add(object);
			}

		} catch (JsonSyntaxException e) {
//			LogUtils.e("not json data:" + json);
		}
		return objects;
	}

	public static String toJson(Object src) {
		Gson gson = new Gson();
		return src == null ? null : gson.toJson(src);
	}


}
