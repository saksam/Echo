package com.example.saksham.echo.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.saksham.echo.Songs
import com.example.saksham.echo.databases.EchoDatabase.statica.COLOMN_SONG_ARTIST
import com.example.saksham.echo.databases.EchoDatabase.statica.COLUMN_ID
import com.example.saksham.echo.databases.EchoDatabase.statica.COLUMN_SONG_PATH
import com.example.saksham.echo.databases.EchoDatabase.statica.COLUMN_SONG_TITLE
import com.example.saksham.echo.databases.EchoDatabase.statica.DB_NAME
import com.example.saksham.echo.databases.EchoDatabase.statica.DB_VERSION
import com.example.saksham.echo.databases.EchoDatabase.statica.TABLE_NAME
import com.example.saksham.echo.fragment.SongPlayingFragment

/**
 * Created by saksham_ on 21-Jul-18.
 */
class EchoDatabase : SQLiteOpenHelper {


    var songList = ArrayList<Songs>()


    object statica {
        val TABLE_NAME = "FavoriteTable"
        val COLUMN_ID = "SongID"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLOMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_PATH = "SongPath"
        var DB_VERSION = 1
        val DB_NAME = "Favorite Database"
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {

        sqLiteDatabase?.execSQL("CREATE TABLE " + statica.TABLE_NAME + "( " + statica.COLUMN_ID + " INTEGER," + statica.COLOMN_SONG_ARTIST + " STRING," + statica.COLUMN_SONG_TITLE + " STRING," + statica.COLUMN_SONG_PATH + " STRING);")

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    constructor(context: Context?) : super(context, DB_NAME, null, DB_VERSION)
    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(context, name, factory, version)

    fun storeAsFavorite(id: Int?, artist: String?, songTitle: String?, path: String?) {

        val db = this.writableDatabase
        var contentValues = ContentValues()
        contentValues.put(statica.COLUMN_ID, id)
        contentValues.put(statica.COLOMN_SONG_ARTIST, artist)
        contentValues.put(statica.COLUMN_SONG_TITLE, songTitle)
        contentValues.put(statica.COLUMN_SONG_PATH, path)
        db.insert(statica.TABLE_NAME, null, contentValues)
        db.close()
    }

    fun queryDBList(): ArrayList<Songs>? {
        try {
            val db = this.readableDatabase
            val query_params = "SELECT * FROM " + statica.TABLE_NAME
            var cSor = db.rawQuery(query_params, null)

            if (cSor.moveToFirst()) {
                do {
                    var _id = cSor.getInt(cSor.getColumnIndexOrThrow(statica.COLUMN_ID))
                    var _artist = cSor.getString(cSor.getColumnIndexOrThrow(statica.COLOMN_SONG_ARTIST))
                    var _title = cSor.getString(cSor.getColumnIndexOrThrow(statica.COLUMN_SONG_TITLE))
                    var _songPath = cSor.getString(cSor.getColumnIndexOrThrow(statica.COLUMN_SONG_PATH))

                    songList.add(Songs(_id.toLong(), _title, _artist, _songPath, 0))
                } while (cSor.moveToNext())
            } else {
                return null
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return songList
    }

    fun checkifIdExists(_id: Int): Boolean {

        /*Random id which does not exist
        * We know that this id can never exist as the song id cannot be less than 0*/
        var storeId = -1090
        val db = this.readableDatabase

        /*The query for checking the if id is present or not is
        * SELECT * FROM FavoriteTable WHERE SongID = <id_of_our_song>*/
        val query_params = "SELECT * FROM " + statica.TABLE_NAME + " WHERE SongID = '$_id'"
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()) {
            do {

                /*Storing the song id into the variable storeId*/
                storeId = cSor.getInt(cSor.getColumnIndexOrThrow(statica.COLUMN_ID))
            } while (cSor.moveToNext())
        } else {
            return false
        }

        /*Here we need to return a boolean value i.e. true or false
        * Hence we check if the store id is not equal to -1090 then we return true, else we return false*/
        return storeId != -1090
    }

    fun deleteFavourite(_id: Int) {
        val db = this.writableDatabase

        /*The delete query is used to perform the delete function*/
        db.delete(statica.TABLE_NAME, statica.COLUMN_ID + " = " + _id, null)

        /*Here is also we close the database connection
        * Note that we only close the database whenever we open in writable mode*/
        db.close()
    }

    fun checkSize(): Int {
        var counter = 0
        val db = this.readableDatabase
        var query_params = "SELECT * FROM " + TABLE_NAME
        /*Here the cursor(cSor) stores the entries returned by the database*/
        val cSor = db.rawQuery(query_params, null)
        /*We add 1 to the counter for every entry*/
        if (cSor.moveToFirst()) {
            do {
                counter = counter + 1
            } while (cSor.moveToNext())
        } else {
            return 0
        }

        /*returning the counter will return the number of elements in the database*/
        return counter
    }
}