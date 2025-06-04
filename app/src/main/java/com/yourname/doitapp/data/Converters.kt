package com.yourname.doitapp.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    companion object {
        private val gson = Gson()

        @JvmStatic
        @TypeConverter
        fun fromStringList(list: List<String>): String = gson.toJson(list)

        @JvmStatic
        @TypeConverter
        fun toStringList(data: String?): List<String> {
            val listType = object : TypeToken<List<String>>() {}.type
            return gson.fromJson(data, listType)
        }

        @JvmStatic
        @TypeConverter
        fun fromBooleanList(list: List<Boolean>): String = gson.toJson(list)

        @JvmStatic
        @TypeConverter
        fun toBooleanList(data: String?): List<Boolean> {
            val listType = object : TypeToken<List<Boolean>>() {}.type
            return gson.fromJson(data, listType)
        }
    }
}
