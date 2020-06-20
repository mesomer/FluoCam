package com.mesomer.fluocam.fileManager

import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.withContext
import java.io.File

class FileDeleter(){
    companion object Delete {
        fun deleteFile(file:File){
            if (file.exists()){
                file.delete()
            }else{
                Log.e("File","文件不存在")
            }
        }
    }
}