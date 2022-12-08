package com.abdurashidov.certificate


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class FileSaveModel {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var fileDest: String = ""
}
