package sample

import io.ktor.utils.io.core.Buffer
import io.ktor.utils.io.core.internal.DangerousInternalIoApi
import kotlinx.io.OutputStream
import kotlinx.serialization.InternalSerializationApi

class FileHleper private constructor(){



    companion object{

        val instance by lazy { FileHleper() }
    }
}