package com.example.saksham.echo

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by saksham_ on 20-Jul-18.
 */
class Songs(var songID: Long,var songTitle: String,var artist: String,var songData: String,var dateAdded:Long):Parcelable{
    override fun writeToParcel(p0: Parcel?, p1: Int) {


    }

    override fun describeContents(): Int {

        return 0;

    }

    object Statified {

        var nameComparator: Comparator<Songs> = Comparator<Songs> { songs1, songs2 ->

            val songOne=songs1.songTitle.toUpperCase()
            val songtwo=songs2.songTitle.toUpperCase()
            songOne.compareTo(songtwo)
        }
        var dateComparator: Comparator<Songs> = Comparator<Songs>{songs1,songs2->

            val songone=songs1.dateAdded.toDouble()
            val songtwo=songs2.dateAdded.toDouble()
            songtwo.compareTo(songone)
        }
    }


}