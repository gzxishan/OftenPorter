package cn.xishan.oftenporter.oftendb.db.asqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import cn.xishan.oftenporter.oftendb.db.DBException;
import cn.xishan.oftenporter.porter.core.util.FileTool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 关于整型字段，都是获取的都是long型的。
 * Created by 宇宙之灵 on 2016/5/3.
 */
class TypeUtil
{
    interface PutDeal<T>
    {
        void put(String name, Object value, T t);
    }

    static class StatementObj
    {
        private SQLiteStatement sqLiteStatement;
        private int index;

        public StatementObj(int index, SQLiteStatement sqLiteStatement)
        {
            this.index = index;
            this.sqLiteStatement = sqLiteStatement;
        }
    }

    static class Type<T> implements Comparable<Type>, PutDeal<T>
    {
        private String cname;
        private PutDeal<T> putDeal;


        public Type(Class<?> c, PutDeal<T> putDeal)
        {
            this.cname = c.getName();
            this.putDeal = putDeal;
        }

        public static Type forSearch(Class<?> c)
        {
            return new Type(c, null);
        }

        @Override
        public int compareTo(Type type)
        {
            return cname.compareTo(type.cname);
        }

        @Override
        public void put(String name, Object value, T t)
        {
            putDeal.put(name, value, t);
        }
    }


    public static Type<ContentValues>[] getTypesForAdd()
    {
        ArrayList<Type<ContentValues>> list = new ArrayList<>(9);
        list.add(new Type<>(Integer.class, new PutDeal<ContentValues>()
        {
            @Override
            public void put(String name, Object value, ContentValues contentValues)
            {
                contentValues.put(name, (Integer) value);
            }
        }));
        list.add(new Type<>(Byte.class, new PutDeal<ContentValues>()
        {
            @Override
            public void put(String name, Object value, ContentValues contentValues)
            {
                contentValues.put(name, (Byte) value);
            }
        }));
        list.add(new Type<>(Float.class, new PutDeal<ContentValues>()
        {
            @Override
            public void put(String name, Object value, ContentValues contentValues)
            {
                contentValues.put(name, (Float) value);
            }
        }));
        list.add(new Type<>(Short.class, new PutDeal<ContentValues>()
        {
            @Override
            public void put(String name, Object value, ContentValues contentValues)
            {
                contentValues.put(name, (Short) value);
            }
        }));
        list.add(new Type<>(String.class, new PutDeal<ContentValues>()
        {
            @Override
            public void put(String name, Object value, ContentValues contentValues)
            {
                contentValues.put(name, (String) value);
            }
        }));
        list.add(new Type<>(CharSequence.class, new PutDeal<ContentValues>()
        {
            @Override
            public void put(String name, Object value, ContentValues contentValues)
            {
                if(value==null){
                    contentValues.put(name, (String) null);
                }else{
                    contentValues.put(name, String.valueOf(value));
                }
            }
        }));
        list.add(new Type<>(Double.class, new PutDeal<ContentValues>()
        {
            @Override
            public void put(String name, Object value, ContentValues contentValues)
            {
                contentValues.put(name, (Double) value);
            }
        }));
        list.add(new Type<>(Long.class, new PutDeal<ContentValues>()
        {
            @Override
            public void put(String name, Object value, ContentValues contentValues)
            {
                contentValues.put(name, (Long) value);
            }
        }));
        list.add(new Type<>(Boolean.class, new PutDeal<ContentValues>()
        {
            @Override
            public void put(String name, Object value, ContentValues contentValues)
            {
                contentValues.put(name, (Boolean) value);
            }
        }));

        list.add(new Type<>(File.class, new PutDeal<ContentValues>()
        {
            @Override
            public void put(String name, Object value, ContentValues contentValues)
            {
                File file = (File) value;
                try
                {
                    contentValues.put(name, FileTool.getData(file,1024));
                } catch (IOException e)
                {
                    throw new DBException(e);
                }
            }
        }));

        Type<ContentValues>[] types = list.toArray(new Type[0]);
        Arrays.sort(types);
        return types;
    }


    public static Type<StatementObj>[] getTypesForMultiAdd()
    {
        ArrayList<Type<StatementObj>> list = new ArrayList<>(9);
        list.add(new Type<>(Integer.class, new PutDeal<StatementObj>()
        {
            @Override
            public void put(String name, Object value, StatementObj statementObj)
            {
                statementObj.sqLiteStatement.bindLong(statementObj.index, (Integer) value);
            }
        }));
        list.add(new Type<>(Byte.class, new PutDeal<StatementObj>()
        {
            @Override
            public void put(String name, Object value, StatementObj statementObj)
            {
                statementObj.sqLiteStatement.bindLong(statementObj.index, (Byte) value);
            }
        }));
        list.add(new Type<>(Float.class, new PutDeal<StatementObj>()
        {
            @Override
            public void put(String name, Object value, StatementObj statementObj)
            {
                statementObj.sqLiteStatement.bindDouble(statementObj.index, (Float) value);
            }
        }));
        list.add(new Type<>(Short.class, new PutDeal<StatementObj>()
        {
            @Override
            public void put(String name, Object value, StatementObj statementObj)
            {
                statementObj.sqLiteStatement.bindLong(statementObj.index, (Short) value);
            }
        }));
        list.add(new Type<>(String.class, new PutDeal<StatementObj>()
        {
            @Override
            public void put(String name, Object value, StatementObj statementObj)
            {
                statementObj.sqLiteStatement.bindString(statementObj.index, (String) value);
            }
        }));
        list.add(new Type<>(CharSequence.class, new PutDeal<StatementObj>()
        {
            @Override
            public void put(String name, Object value, StatementObj statementObj)
            {
                if(value==null){
                    statementObj.sqLiteStatement.bindString(statementObj.index, (String) value);
                }else{
                    statementObj.sqLiteStatement.bindString(statementObj.index, String.valueOf(value));
                }
            }
        }));
        list.add(new Type<>(Double.class, new PutDeal<StatementObj>()
        {
            @Override
            public void put(String name, Object value, StatementObj statementObj)
            {
                statementObj.sqLiteStatement.bindDouble(statementObj.index, (Double) value);
            }
        }));
        list.add(new Type<>(Long.class, new PutDeal<StatementObj>()
        {
            @Override
            public void put(String name, Object value, StatementObj statementObj)
            {
                statementObj.sqLiteStatement.bindLong(statementObj.index, (Long) value);
            }
        }));
        list.add(new Type<>(Boolean.class, new PutDeal<StatementObj>()
        {
            @Override
            public void put(String name, Object value, StatementObj statementObj)
            {
                statementObj.sqLiteStatement.bindLong(statementObj.index, (Boolean) value ? 1 : 0);
            }
        }));
        list.add(new Type<>(byte[].class, new PutDeal<StatementObj>()
        {
            @Override
            public void put(String name, Object value, StatementObj statementObj)
            {
                statementObj.sqLiteStatement.bindBlob(statementObj.index, (byte[]) value);
            }
        }));

        Type<StatementObj>[] types = list.toArray(new Type[0]);
        Arrays.sort(types);
        return types;
    }


    static Object getObject(Cursor cursor, int columnIndex) throws Exception
    {
        int type = getType(cursor, columnIndex);
        Object value;
        switch (type)
        {
            case Cursor.FIELD_TYPE_BLOB:
                value = cursor.getBlob(columnIndex);
                break;
            case Cursor.FIELD_TYPE_FLOAT:
                value = cursor.getFloat(columnIndex);
                break;
            case Cursor.FIELD_TYPE_INTEGER:
                value = cursor.getLong(columnIndex);
                break;
            case Cursor.FIELD_TYPE_NULL:
                value = null;
                break;
            case Cursor.FIELD_TYPE_STRING:
                value = cursor.getString(columnIndex);
                break;
            default:
                value = cursor.getString(columnIndex);
        }
        return value;
    }

    private static int getType(Cursor cursor, int columnIndex) throws Exception
    {

        if (Build.VERSION.SDK_INT >= 11)
        {
            return cursor.getType(columnIndex);
        }

        SQLiteCursor sqLiteCursor = (SQLiteCursor) cursor;
        CursorWindow cursorWindow = sqLiteCursor.getWindow();
        int pos = cursor.getPosition();
        int type = -1;
        if (cursorWindow.isNull(pos, columnIndex))
        {
            type = Cursor.FIELD_TYPE_NULL;
        } else if (cursorWindow.isLong(pos, columnIndex))
        {
            type = Cursor.FIELD_TYPE_INTEGER;
        } else if (cursorWindow.isFloat(pos, columnIndex))
        {
            type = Cursor.FIELD_TYPE_FLOAT;
        } else if (cursorWindow.isString(pos, columnIndex))
        {
            type = Cursor.FIELD_TYPE_STRING;
        } else if (cursorWindow.isBlob(pos, columnIndex))
        {
            type = Cursor.FIELD_TYPE_BLOB;
        }

        return type;
    }

}
