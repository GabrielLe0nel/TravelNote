package br.com.travelnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class MemoryDatabase extends SQLiteOpenHelper {
    public MemoryDatabase(Context context) { super(context, "travelnote.db", null, 4); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE memories (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, description TEXT NOT NULL, photo_uri TEXT, latitude REAL, longitude REAL, location_name TEXT, created_at TEXT NOT NULL, pressure_hpa REAL)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) db.execSQL("ALTER TABLE memories ADD COLUMN location_name TEXT");
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE memories_migrated (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, description TEXT NOT NULL, photo_uri TEXT, latitude REAL, longitude REAL, location_name TEXT, created_at TEXT NOT NULL)");
            db.execSQL("INSERT INTO memories_migrated (id, title, description, photo_uri, latitude, longitude, location_name, created_at) SELECT id, title, description, photo_uri, latitude, longitude, location_name, created_at FROM memories");
            db.execSQL("DROP TABLE memories");
            db.execSQL("ALTER TABLE memories_migrated RENAME TO memories");
        }
        if (oldVersion < 4) db.execSQL("ALTER TABLE memories ADD COLUMN pressure_hpa REAL");
    }
    public long save(Memory m) {
        ContentValues v = values(m);
        SQLiteDatabase db = getWritableDatabase();
        if (m.id == 0) { long id=db.insert("memories", null, v); if(id>0)m.id=id; return id; }
        return db.update("memories", v, "id=?", new String[]{String.valueOf(m.id)}) > 0 ? m.id : -1;
    }
    private ContentValues values(Memory m) {
        ContentValues v = new ContentValues(); v.put("title", m.title); v.put("description", m.description);
        v.put("photo_uri", m.photoUri); if (m.latitude == null) v.putNull("latitude"); else v.put("latitude", m.latitude);
        if (m.longitude == null) v.putNull("longitude"); else v.put("longitude", m.longitude);
        v.put("location_name",m.locationName); v.put("created_at", m.createdAt);
        if (m.pressureHpa == null) v.putNull("pressure_hpa"); else v.put("pressure_hpa", m.pressureHpa);
        return v;
    }
    public List<Memory> all() {
        List<Memory> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT * FROM memories ORDER BY id DESC", null)) { while (c.moveToNext()) list.add(read(c)); }
        return list;
    }
    public Memory find(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT * FROM memories WHERE id=?", new String[]{String.valueOf(id)})) { return c.moveToFirst() ? read(c) : null; }
    }
    public void delete(long id) { getWritableDatabase().delete("memories", "id=?", new String[]{String.valueOf(id)}); }
    public void updateLocationName(long id,String name) { ContentValues values=new ContentValues(); values.put("location_name",name); getWritableDatabase().update("memories",values,"id=?",new String[]{String.valueOf(id)}); }
    private Memory read(Cursor c) {
        Memory m = new Memory(); m.id=c.getLong(c.getColumnIndexOrThrow("id")); m.title=c.getString(c.getColumnIndexOrThrow("title"));
        m.description=c.getString(c.getColumnIndexOrThrow("description")); m.photoUri=c.getString(c.getColumnIndexOrThrow("photo_uri")); m.locationName=c.getString(c.getColumnIndexOrThrow("location_name"));
        int lat=c.getColumnIndexOrThrow("latitude"), lon=c.getColumnIndexOrThrow("longitude");
        if (!c.isNull(lat)) m.latitude=c.getDouble(lat); if (!c.isNull(lon)) m.longitude=c.getDouble(lon);
        int pressure=c.getColumnIndexOrThrow("pressure_hpa");
        if (!c.isNull(pressure)) m.pressureHpa=c.getFloat(pressure);
        m.createdAt=c.getString(c.getColumnIndexOrThrow("created_at")); return m;
    }
}
